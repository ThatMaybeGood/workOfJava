package com.showexcel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import com.showexcel.controller.CashStatisticsController;
import com.showexcel.service.CashStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.showexcel.dto.CashStatisticsTableDTO;

@ExtendWith(MockitoExtension.class)
class CashStatisticsControllerTest {

    @Mock
    private CashStatisticsService cashStatisticsService;

    @InjectMocks
    private CashStatisticsController cashStatisticsController;

    private List<CashStatisticsTableDTO> mockResult;

    @BeforeEach
    void setUp() {
        CashStatisticsTableDTO dto1 = new CashStatisticsTableDTO();
        CashStatisticsTableDTO dto2 = new CashStatisticsTableDTO();
        mockResult = Arrays.asList(dto1, dto2);
    }

    @Test
    void getCashStatisticsTable_ShouldReturnListOfCashStatisticsTableDTO() {
        when(cashStatisticsService.getAllStatisticsTable()).thenReturn(mockResult);

        List<CashStatisticsTableDTO> result = cashStatisticsController.getCashStatisticsTable();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(mockResult, result);
    }

    @Test
    void getCashStatisticsTable_ShouldReturnEmptyListWhenNoData() {
        when(cashStatisticsService.getAllStatisticsTable()).thenReturn(Arrays.asList());

        List<CashStatisticsTableDTO> result = cashStatisticsController.getCashStatisticsTable();

        assertNotNull(result);
        assertEquals(0, result.size());
    }
}