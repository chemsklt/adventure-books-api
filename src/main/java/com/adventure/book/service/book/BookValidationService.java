package com.adventure.book.service.book;

import com.adventure.book.domain.book.Book;
import com.adventure.book.domain.book.Option;
import com.adventure.book.domain.book.Section;
import com.adventure.book.domain.book.SectionType;
import com.adventure.book.exception.book.InvalidBookException;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BookValidationService {

    public void validate(Book book) {
        List<Section> sections = book.getSections();

        if (sections == null || sections.isEmpty()) {
            throw new InvalidBookException("Book '%s' has no sections".formatted(book.getId()));
        }

        long beginCount = sections.stream()
                .filter(section -> section.getType() == SectionType.BEGIN)
                .count();

        if (beginCount != 1) {
            throw new InvalidBookException(
                    "Book '%s' must contain exactly one BEGIN section but found %d"
                            .formatted(book.getId(), beginCount)
            );
        }

        Map<String, Section> sectionsById = new HashMap<>();
        for (Section section : sections) {
            sectionsById.put(section.getId(), section);
        }

        Section beginSection = sections.stream()
                .filter(section -> section.getType() == SectionType.BEGIN)
                .findFirst()
                .orElseThrow(() -> new InvalidBookException(
                        "Book '%s' must contain a BEGIN section".formatted(book.getId())
                ));

        Set<String> reachableIds = collectReachableSectionIds(beginSection, sectionsById, book.getId());

        boolean hasReachableEnd = reachableIds.stream()
                .map(sectionsById::get)
                .anyMatch(section -> section.getType() == SectionType.END);

        if (!hasReachableEnd) {
            throw new InvalidBookException(
                    "Book '%s' must contain at least one reachable END section".formatted(book.getId())
            );
        }

        for (String sectionId : reachableIds) {
            Section section = sectionsById.get(sectionId);
            boolean isEnd = section.getType() == SectionType.END;

            if (!isEnd && (section.getOptions() == null || section.getOptions().isEmpty())) {
                throw new InvalidBookException(
                        "Section '%s' in book '%s' is not END and must contain at least one option"
                                .formatted(section.getId(), book.getId())
                );
            }
        }
    }

    private Set<String> collectReachableSectionIds(
            Section beginSection,
            Map<String, Section> sectionsById,
            String bookId
    ) {
        Set<String> visited = new HashSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(beginSection.getId());

        while (!queue.isEmpty()) {
            String currentId = queue.poll();

            if (!visited.add(currentId)) {
                continue;
            }

            Section currentSection = sectionsById.get(currentId);
            if (currentSection == null) {
                throw new InvalidBookException(
                        "Book '%s' references missing section '%s'".formatted(bookId, currentId)
                );
            }

            if (currentSection.getOptions() == null) {
                continue;
            }

            for (Option option : currentSection.getOptions()) {
                String gotoId = option.getGotoId();

                if (gotoId == null || gotoId.isBlank()) {
                    throw new InvalidBookException(
                            "Section '%s' in book '%s' contains an option without gotoId"
                                    .formatted(currentSection.getId(), bookId)
                    );
                }

                if (!sectionsById.containsKey(gotoId)) {
                    throw new InvalidBookException(
                            "Section '%s' in book '%s' references unknown gotoId '%s'"
                                    .formatted(currentSection.getId(), bookId, gotoId)
                    );
                }

                queue.add(gotoId);
            }
        }

        return visited;
    }
}