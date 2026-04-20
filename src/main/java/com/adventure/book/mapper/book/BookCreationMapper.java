package com.adventure.book.mapper.book;

import com.adventure.book.domain.book.Book;
import com.adventure.book.domain.Consequence;
import com.adventure.book.domain.ConsequenceType;
import com.adventure.book.domain.book.Difficulty;
import com.adventure.book.domain.book.Option;
import com.adventure.book.domain.book.Section;
import com.adventure.book.domain.book.SectionType;
import com.adventure.book.generated.model.CreateBookRequest;
import com.adventure.book.generated.model.CreateConsequenceRequest;
import com.adventure.book.generated.model.CreateOptionRequest;
import com.adventure.book.generated.model.CreateSectionRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BookCreationMapper {

    @Mapping(target = "difficulty", expression = "java(toDifficulty(request.getDifficulty()))")
    @Mapping(target = "categories", expression = "java(normalizeCategories(request.getCategories()))")
    @Mapping(target = "sections", source = "sections")
    Book toBook(CreateBookRequest request);

    @Mapping(target = "type", expression = "java(toSectionType(request.getType()))")
    @Mapping(target = "options", source = "options")
    Section toSection(CreateSectionRequest request);

    @Mapping(target = "consequence", source = "consequence")
    Option toOption(CreateOptionRequest request);

    @Mapping(target = "type", expression = "java(toConsequenceType(request.getType()))")
    Consequence toConsequence(CreateConsequenceRequest request);

    List<Section> toSections(List<CreateSectionRequest> requests);

    List<Option> toOptions(List<CreateOptionRequest> requests);

    default Difficulty toDifficulty(com.adventure.book.generated.model.Difficulty difficulty) {
        if (difficulty == null) {
            return null;
        }
        return Difficulty.valueOf(difficulty.getValue());
    }

    default SectionType toSectionType(CreateSectionRequest.TypeEnum type) {
        if (type == null) {
            return null;
        }
        return SectionType.valueOf(type.getValue());
    }

    default ConsequenceType toConsequenceType(com.adventure.book.generated.model.ConsequenceType type) {
        if (type == null) {
            return null;
        }
        return ConsequenceType.valueOf(type.getValue());
    }

    default Set<String> normalizeCategories(List<String> categories) {
        if (categories == null) {
            return new LinkedHashSet<>();
        }

        return categories.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
