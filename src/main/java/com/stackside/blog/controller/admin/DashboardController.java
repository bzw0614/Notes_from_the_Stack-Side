package com.stackside.blog.controller.admin;

import com.stackside.blog.common.result.Result;
import com.stackside.blog.model.vo.DashboardStatsVO;
import com.stackside.blog.service.ArticleAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin dashboard APIs")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/dashboard")
public class DashboardController {

    private final ArticleAdminService articleAdminService;

    @Operation(summary = "Get dashboard stats")
    @GetMapping("/stats")
    public Result<DashboardStatsVO> stats() {
        return Result.success(articleAdminService.getDashboardStats());
    }
}
