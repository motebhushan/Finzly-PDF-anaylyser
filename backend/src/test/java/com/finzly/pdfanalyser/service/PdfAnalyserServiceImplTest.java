package com.finzly.pdfanalyser.service;

import com.finzly.pdfanalyser.client.GeminiClient;
import com.finzly.pdfanalyser.dto.request.PdfAnalyseRequest;
import com.finzly.pdfanalyser.dto.response.PdfAnalysisResponse;
import com.finzly.pdfanalyser.exception.InvalidPdfUrlException;
import com.finzly.pdfanalyser.util.PdfUrlValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit test for PdfAnalyserServiceImpl.
 *
 * @ExtendWith(MockitoExtension) enables pure Mockito without Spring context.
 * This keeps tests blazing fast and isolated to the service logic only.
 */
@ExtendWith(MockitoExtension.class)
class PdfAnalyserServiceImplTest {

    @Mock
    private PdfUrlValidator pdfUrlValidator;

    @Mock
    private GeminiClient geminiClient;

    @InjectMocks
    private PdfAnalyserServiceImpl pdfAnalyserService;

    @Test
    @DisplayName("analyse - validates URL before calling LLM client")
    void analyse_validatesUrlFirst() {
        String url = "https://arxiv.org/pdf/1706.03762";
        PdfAnalyseRequest request = new PdfAnalyseRequest(url);
        PdfAnalysisResponse mockResponse = new PdfAnalysisResponse(
                "Research Paper", "Title", "Author", "Summary", "Takeaway");

        when(geminiClient.analyse(url)).thenReturn(mockResponse);

        PdfAnalysisResponse result = pdfAnalyserService.analyse(request);

        // Verify validator was called BEFORE the LLM client
        verify(pdfUrlValidator, times(1)).validate(url);
        verify(geminiClient, times(1)).analyse(url);
        assertThat(result.title()).isEqualTo("Title");
    }

    @Test
    @DisplayName("analyse - throws InvalidPdfUrlException when validation fails")
    void analyse_throwsWhenValidationFails() {
        String url = "https://example.com/page";
        PdfAnalyseRequest request = new PdfAnalyseRequest(url);

        doThrow(new InvalidPdfUrlException("Not a PDF"))
                .when(pdfUrlValidator).validate(url);

        assertThatThrownBy(() -> pdfAnalyserService.analyse(request))
                .isInstanceOf(InvalidPdfUrlException.class)
                .hasMessage("Not a PDF");

        // LLM client must NOT be called if validation fails
        verifyNoInteractions(geminiClient);
    }
}
