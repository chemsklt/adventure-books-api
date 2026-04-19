package com.adventure.book.mapper;

import com.adventure.book.domain.Option;
import com.adventure.book.domain.Section;
import com.adventure.book.domain.SectionType;
import com.adventure.book.generated.model.SectionResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookReadingMapperTest {

    private final BookReadingMapper mapper = new BookReadingMapper();

    @Test
    void shouldMapSectionToSectionResponse() {
        Section section = new Section(
                "1",
                "Start text",
                SectionType.BEGIN,
                List.of(
                        new Option("Open the door", "500", null),
                        new Option("Look under the bed", "20", null)
                )
        );

        SectionResponse response = mapper.toSectionResponse(section);

        assertThat(response.getSectionId()).isEqualTo("1");
        assertThat(response.getText()).isEqualTo("Start text");
        assertThat(response.getType()).isEqualTo(SectionResponse.TypeEnum.BEGIN);
        assertThat(response.getOptions()).hasSize(2);
        assertThat(response.getOptions().get(0).getId()).isEqualTo("0");
        assertThat(response.getOptions().get(0).getText()).isEqualTo("Open the door");
        assertThat(response.getOptions().get(0).getNextSectionId()).isEqualTo("500");
        assertThat(response.getOptions().get(1).getId()).isEqualTo("1");
    }

    @Test
    void shouldMapSectionWithoutOptionsToEmptyOptionsList() {
        Section section = new Section("1000", "End", SectionType.END, null);

        SectionResponse response = mapper.toSectionResponse(section);

        assertThat(response.getSectionId()).isEqualTo("1000");
        assertThat(response.getOptions()).isEmpty();
    }
}