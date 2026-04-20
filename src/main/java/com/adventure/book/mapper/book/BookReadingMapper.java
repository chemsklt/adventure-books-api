package com.adventure.book.mapper.book;

import com.adventure.book.domain.book.Option;
import com.adventure.book.domain.book.Section;
import com.adventure.book.domain.book.SectionType;
import com.adventure.book.generated.model.OptionResponse;
import com.adventure.book.generated.model.SectionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface BookReadingMapper {

    @Mapping(target = "sectionId", source = "id")
    @Mapping(target = "type", source = "type")
    @Mapping(target = "options", expression = "java(mapOptions(section.getOptions()))")
    SectionResponse toSectionResponse(Section section);

    default SectionResponse.TypeEnum map(SectionType type) {
        return type == null ? null : SectionResponse.TypeEnum.valueOf(type.name());
    }

    default List<OptionResponse> mapOptions(List<Option> options) {
        List<OptionResponse> responses = new ArrayList<>();

        if (options == null) {
            return responses;
        }

        for (int i = 0; i < options.size(); i++) {
            Option option = options.get(i);

            OptionResponse response = new OptionResponse()
                    .id(String.valueOf(i))
                    .text(option.getDescription())
                    .nextSectionId(option.getGotoId());

            responses.add(response);
        }

        return responses;
    }
}