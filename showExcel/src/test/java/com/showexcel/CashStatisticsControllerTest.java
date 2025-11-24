package com.showexcel;

import com.showexcel.controller.CashStatisticsController;
import com.showexcel.dto.CashStatisticsTableDTO;
import com.showexcel.service.CashStatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CashStatisticsControllerTest {

    private static final Logger log = LoggerFactory.getLogger(CashStatisticsControllerTest.class);
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