package com.google.code.regexp;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;

public class Patterns {

	public final static List<String>
		patterns = of(
			"(?<protocol>https?://[^/]+)?/navig/0,\\d*,s(?<siteId>\\d{1,2})_l\\d_d\\d_r(?<rubriqueId>\\d+),00.html(?<anchor>#.*)?",
			"href=\"javascript\\:openURL\\((?<migrationId>\\d+),(?<type>\\d),'(?<extension>\\w+)'\\)\"",
			"src=\"(?<url>/download/(?<fileName>\\S*\\.(?<extension>gif|bmp|jpeg|jpg|png)))\"");

	public final static List<List<String>>
		groupNames = of(
				(List<String>) of("protocol", "siteId", "rubriqueId", "anchor"),
				(List<String>) of("migrationId", "type", "extension"),
				(List<String>) of("url", "fileName", "extension")
			);

	public static final List<String>
		standardPatterns = of(
			"(https?://[^/]+)?/navig/0,\\d*,s(\\d{1,2})_l\\d_d\\d_r(\\d+),00.html(#.*)?",
			"href=\"javascript\\:openURL\\((\\d+),(\\d),'(\\w+)'\\)\"",
			"src=\"(/download/(\\S*\\.(gif|bmp|jpeg|jpg|png)))\"");

	public static final List<List<String>>
		goodInputs = of(
			(List<String>) of(
					"/navig/0,2158,s31_l1_d0_r3486,00.html",
					"http://www.google.com/navig/0,2158,s31_l1_d0_r3490,00.html#section_test",
					"https://www.google.fr/navig/0,2599,s31_l1_d0_r3312,00.html"),
			(List<String>) of(
					"href=\"javascript:openURL(137651,6,'pdf')\"",
					"href=\"javascript:openURL(284694,6,'pdf')\"",
					"href=\"javascript:openURL(40414,4,'html')\""),
			(List<String>) of(
					"src=\"/download/0,680270,120835_1,00.gif\"",
					"src=\"/download/0,,11877_1,00.gif\"",
					"src=\"/download/0,671975,107350_1,00.jpg\"")
			);

	public static final List<List<String>>
       badInputs = of(
    	   (List<String>) of(
		    	   "http://www.google.fr/naig/0,2599,s31_l1_d0_r3312,00.html"),
    	   (List<String>) of(
		    	   "href=\"javascript:openRL(40414,4,'html')\""),
    	   (List<String>) of(
    			   "src=\"/downlod/0,680270,120835_1,00.gif\"")
		   );

}
