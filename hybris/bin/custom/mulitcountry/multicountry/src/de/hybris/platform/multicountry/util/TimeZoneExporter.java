package de.hybris.platform.multicountry.util;

import de.hybris.platform.multicountry.enums.TimezoneEnum;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.StreamUtils;

/**
 * Utility class to be used to generate a new timezones.impex file.
 */
public final class TimeZoneExporter
{
	/**
	 * Exports the timezone data to an impex file. The ordering is determined by the system.
	 *
	 * @param outputPath The complete path to the output file, not empty, blank or null
	 * @param languages A comma-delimited list of languages, not empty, blank or null
	 * @throws IOException An IO exception
	 * @throws IllegalArgumentException An illegal argument was provided
	 */
	public static void exportToImpex(final String outputPath, final String languages) throws IOException, IllegalArgumentException
	{
		if (StringUtils.isBlank(outputPath))
		{
			throw new IllegalArgumentException("The outputPath must contain a valid file path");
		}

		if (StringUtils.isBlank(languages))
		{
			throw new IllegalArgumentException("The languages must contain at least one language isocode");
		}

		final BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(new File(outputPath)));
		outputHeader(os, languages);
		outputTimezoneLines(os, languages);
		os.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
		os.flush();
		os.close();
	}

	/**
	 * Outputs the header line for a TimezoneEnum type with the name in each of the requested languages.
	 *
	 * @param os The output stream
	 * @param languages The requested languages
	 * @throws IOException An IO exception
	 */
	protected static void outputHeader(final BufferedOutputStream os, final String languages) throws IOException
	{
		final String headerStart = "INSERT_UPDATE " + TimezoneEnum._TYPECODE + ";code[unique=true];";
		final String languageFormat = "name[lang=%s]";
		final String header =
				Stream.of(languages.split(",")).map(lang -> String.format(languageFormat, lang)).collect(Collectors.joining(";", headerStart, System.lineSeparator()));
		os.write(header.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Outputs the timezone lines to the file with the name for each requested language.
	 *
	 * @param os The output stream
	 * @param languages The requested languages
	 * @throws IOException An IO exception
	 */
	protected static void outputTimezoneLines(final BufferedOutputStream os, final String languages) throws IOException
	{
		final List<String> lines = Stream.of(TimeZone.getAvailableIDs())
				.map(tzId -> generateLineForTimeZone(tzId, languages))
				.distinct()
				.sorted()
				.collect(Collectors.toList());

		for (final String line : lines)
		{
			os.write(line.getBytes(StandardCharsets.UTF_8));
		}
	}

	/**
	 * Generates a timezone line. The TimezoneEnum code is the id of the ZoneId for the timezone, and for each language the
	 * localized display name + GMT offset is output. For example, in English and French:
	 *
	 * <pre>;America/Edmonton;Mountain Standard Time (GMT-07:00);Heure normale des Rocheuses (GMT-07:00)</pre>
	 *
	 * @param tzId The timezone id
	 * @param languages The requested languages
	 * @return The generated line
	 */
	protected static String generateLineForTimeZone(final String tzId, final String languages)
	{
		final String tzFormat = "\"%s (%s GMT%s)\"";
		final TimeZone tz = TimeZone.getTimeZone(tzId);
		final String lineStart = ";\"" + tz.toZoneId().getId() + "\";";
		final ZoneOffset offset = ZoneOffset.ofTotalSeconds(tz.getRawOffset() / 1000);
		return Stream.of(languages.split(",")).map(
				lang ->
				{
					final Locale locale = new Locale.Builder().setLanguage(lang).build();
					return String.format(tzFormat, tz.toZoneId().getId(), tz.getDisplayName(false, TimeZone.LONG, locale), offset);
				}
		).collect(Collectors.joining(";", lineStart, System.lineSeparator()));
	}

	/**
	 * Execute with the following parameters:
	 * <ul>
	 *     <li>path - Full path to the output file</li>
	 *     <li>isocodes - Comma-separated list of language isocodes for the output</li>
	 * </ul>
	 * @param args The arguments
	 */
	public static void main(String[] args)
	{
		if (args.length < 2)
		{
			System.out.println("Please provide the full file path and the list of output languages isocodes");
			return;
		}

		try
		{
			TimeZoneExporter.exportToImpex(args[0], args[1]);
		}
		catch (final IOException ex)
		{
			System.out.println(ex.getMessage());
		}
	}
}
