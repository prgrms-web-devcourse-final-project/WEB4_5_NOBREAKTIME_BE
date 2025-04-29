package com.mallang.mallang_backend.domain.video.video.service.impl;

import lombok.Value;

@Value
public class SearchContext {
	String query;
	String region;
	String langKey;
	String category;
	boolean defaultSearch;
}