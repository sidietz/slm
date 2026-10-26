package de.oberamsystems.slm.converter;

import java.time.OffsetDateTime;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import de.oberamsystems.slm.config.TimeZoneConfig;

@Converter(autoApply = true)
public class OffsetDateTimeAttributeConverter implements AttributeConverter<OffsetDateTime, OffsetDateTime> {

	@Override
	public OffsetDateTime convertToDatabaseColumn(OffsetDateTime attribute) {
		return attribute;
	}

	@Override
	public OffsetDateTime convertToEntityAttribute(OffsetDateTime dbData) {
		if (dbData == null) {
			return null;
		}
		return dbData.atZoneSameInstant(TimeZoneConfig.getZoneId()).toOffsetDateTime();
	}
}
