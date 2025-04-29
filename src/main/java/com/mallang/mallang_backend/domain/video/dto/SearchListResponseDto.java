package com.mallang.mallang_backend.domain.video.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchListResponseDto {
	private List<VideoResponse> items;
	private String nextPageToken;
	private String prevPageToken;
	private PageInfo pageInfo;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class PageInfo {
		private int totalResults;
		private int resultsPerPage;
	}
}
