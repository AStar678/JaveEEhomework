package com.group.viewer.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TopViewerDTO {
    private Long viewerId;
    private String viewerName;
    private BigDecimal totalAmount;
}
