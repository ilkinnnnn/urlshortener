package io.github.ilkinnnnn.urlshortener.controller;

import io.github.ilkinnnnn.urlshortener.model.response.PageResponse;
import io.github.ilkinnnnn.urlshortener.model.response.UrlResponseWithUserId;
import io.github.ilkinnnnn.urlshortener.service.UrlService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/admin")
@AllArgsConstructor
public class AdminController {
    private final UrlService urlService;

    @GetMapping("/short-urls")
    public PageResponse<UrlResponseWithUserId> getAllAdmin(Pageable pageable) {
        return urlService.getAllAdmin(pageable);
    }
}
