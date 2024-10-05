package com.thd.mapserver.infrastructure.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingPageController {

	@GetMapping("/")
	public String test(Model model) {
		final var collections = List.of(new FeatureCollection("Punkte", "http://localhost:8080/features"),
				new FeatureCollection("Punkte", "http://localhost:8080/features/{Gemeinde}"),
				new FeatureCollection("hexagon", "http://localhost:8080/hexagon"),
				new FeatureCollection("Polygon", "http://localhost:8080/polygon"));
		model.addAttribute("collections", collections); // A Attribute can be accessed via the ${attributeName} syntax
														// in the html template
		return "index"; // name of the template page located under resources/templates
	}

	@GetMapping("/conformance")
	public String getLandingPage() {
		return "conformance";
	}
	
	public static class FeatureCollection {
		private String name;
		private String description;

		public FeatureCollection(String name, String description) {
			this.name = name;
			this.description = description;
		}
		
		public String getName() {
			return this.name;
		}
		
		public String getDescription() {
			return this.description;
		}
	}

}
