package com.openkm.test.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("title", "MCP Server Test");
		model.addAttribute("description", "");
		model.addAttribute("serverPort", "8080");
		model.addAttribute("contextPath", "/openkm");
		return "home";
	}
}
