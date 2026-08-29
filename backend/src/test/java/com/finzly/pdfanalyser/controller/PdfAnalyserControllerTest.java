package com.finzly.pdfanalyser.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finzly.pdfanalyser.dto.request.PdfAnalyseRequest;
import com.finzly.pdfanalyser.dto.response.PdfAnalysisResponse;
import com.finzly.pdfanalyser.exception.GlobalExceptionHandler;
import com.finzly.pdfanalyser.exception.InvalidPdfUrlException;
import com.finzly.pdfanalyser.service.PdfAnalyserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Slice test for PdfAnalyserController.
 *
 * @WebMvcTest loads only the web layer (controller + filters + exception handlers).
 * The service layer is mocked, so this test is fast and focused.
 */
@WebMvcTest({PdfAnalyserController.class, GlobalExceptionHandler.class})
class PdfAnalyserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PdfAnalyserService pdfAnalyserService;

    @Test
    @DisplayName("POST /api/v1/pdf/analyse - returns 200 with analysis on valid request")
    void analyse_validRequest_returns200() throws Exception {
        PdfAnalyserRequest request = new PdfAnalyseRequest("https://arxiv.org/pdf/1706.03762");
        PdfAnalysisResponse response = new PdfAnalysisResponse(
                "Research Paper",
                "Attention Is All You Need",
                "Vaswani et al.",
                "This paper proposes the Transformer architecture.",
                "Self-attention replaces recurrence entirely."
        );

        when(pdfAnalyserService.analyse(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/pdf/analyse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Attention Is All You Need"))
                .andExpect(jsonPath("$.documentType").value("Research Paper"))
                .andExpect(jsonPath("$.authors").value("Vaswani et al."));
    }

    @Test
    @DisplayName("POST /api/v1/pdf/analyse - returns 400 when URL is blank")
    void analyse_blankUrl_returns400() throws Exception {
        PdfAnalyseRequest request = new PdfAnalyseRequest("");

        mockMvc.perform(post("/api/v1/pdf/analyse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/pdf/analyse - returns 400 on invalid PDF URL from service")
    void analyse_invalidPdfUrl_returns400() throws Exception {
        PdfAnalyseRequest request = new PdfAnalyseRequest("https://example.com/not-a-pdf");

        when(pdfAnalyserService.analyse(any()))
                .thenThrow(new InvalidPdfUrlException("URL does not point to a valid PDF."));

        mockMvc.perform(post("/api/v1/pdf/analyse")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("URL does not point to a valid PDF."));
    }

    @Test
    @DisplayName("GET /api/v1/pdf/health - returns 200")
    void health_returns200() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/v1/pdf/health"))
                .andExpect(status().isOk());
    }

    // Convenience alias to match the record name
    private record PdfAnalyserRequest(String pdfUrl) {}
}
