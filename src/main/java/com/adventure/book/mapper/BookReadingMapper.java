package com.adventure.book.mapper;

import com.adventure.book.domain.Option;
import com.adventure.book.domain.Section;
import com.adventure.book.generated.model.OptionResponse;
import com.adventure.book.generated.model.SectionResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class BookReadingMapper {

    public SectionResponse toSectionResponse(Section section) {
        SectionResponse response = new SectionResponse()
                .sectionId(section.getId())
                .text(section.getText())
                .type(SectionResponse.TypeEnum.valueOf(section.getType().name()));

        List<OptionResponse> options = new ArrayList<>();

        if (section.getOptions() != null) {
            for (int i = 0; i < section.getOptions().size(); i++) {
                Option option = section.getOptions().get(i);

                OptionResponse optionResponse = new OptionResponse()
                        .id(String.valueOf(i))
                        .text(option.getDescription())
                        .nextSectionId(option.getGotoId());

                options.add(optionResponse);
            }
        }

        response.setOptions(options);
        return response;
    }
}