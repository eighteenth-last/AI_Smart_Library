package com.library.module.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.library.module.ai.dto.KnowledgeImportDTO;
import com.library.module.ai.entity.KnowledgeBase;
import com.library.module.ai.mapper.KnowledgeBaseMapper;
import com.library.module.ai.service.KnowledgeImportExportService;
import com.library.module.ai.vo.ImportResultVO;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 知识库导入导出服务实现类
 */
@Slf4j
@Service
public class KnowledgeImportExportServiceImpl implements KnowledgeImportExportService {
    
    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;
    
    private static final String[] EXCEL_HEADERS = {"问题", "答案", "分类", "来源", "是否公开"};
    private static final String[] CSV_HEADERS = {"question", "answer", "category", "source", "is_public"};
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVO importKnowledge(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("文件名不能为空");
        }
        
        List<KnowledgeImportDTO> importList;
        
        // 根据文件类型解析
        if (filename.endsWith(".xlsx") || filename.endsWith(".xls")) {
            importList = parseExcel(file.getInputStream());
        } else if (filename.endsWith(".csv")) {
            importList = parseCSV(file.getInputStream());
        } else {
            throw new IllegalArgumentException("不支持的文件格式，仅支持 .xlsx, .xls, .csv");
        }
        
        // 批量导入
        return batchImport(importList);
    }
    
    /**
     * 解析 Excel 文件
     */
    private List<KnowledgeImportDTO> parseExcel(InputStream inputStream) throws IOException {
        List<KnowledgeImportDTO> list = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 跳过标题行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                KnowledgeImportDTO dto = new KnowledgeImportDTO();
                dto.setRowNumber(i + 1); // Excel 行号从 1 开始
                
                dto.setQuestion(getCellValue(row.getCell(0)));
                dto.setAnswer(getCellValue(row.getCell(1)));
                dto.setCategory(getCellValue(row.getCell(2)));
                dto.setSource(getCellValue(row.getCell(3)));
                
                String isPublicStr = getCellValue(row.getCell(4));
                dto.setIsPublic(parseIsPublic(isPublicStr));
                
                list.add(dto);
            }
        }
        
        log.info("Excel 解析完成，共 {} 条记录", list.size());
        return list;
    }
    
    /**
     * 解析 CSV 文件
     */
    private List<KnowledgeImportDTO> parseCSV(InputStream inputStream) throws IOException {
        List<KnowledgeImportDTO> list = new ArrayList<>();
        
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String[] headers = csvReader.readNext(); // 跳过标题行
            
            String[] line;
            int rowNumber = 2; // CSV 行号从 2 开始（1 是标题）
            while ((line = csvReader.readNext()) != null) {
                if (line.length < 5) {
                    log.warn("第 {} 行数据不完整，跳过", rowNumber);
                    rowNumber++;
                    continue;
                }
                
                KnowledgeImportDTO dto = new KnowledgeImportDTO();
                dto.setRowNumber(rowNumber);
                dto.setQuestion(line[0]);
                dto.setAnswer(line[1]);
                dto.setCategory(line[2]);
                dto.setSource(line[3]);
                dto.setIsPublic(parseIsPublic(line[4]));
                
                list.add(dto);
                rowNumber++;
            }
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }

        log.info("CSV 解析完成，共 {} 条记录", list.size());
        return list;
    }
    
    /**
     * 批量导入知识库
     */
    private ImportResultVO batchImport(List<KnowledgeImportDTO> importList) {
        ImportResultVO result = new ImportResultVO();
        result.setTotalCount(importList.size());
        result.setSuccessCount(0);
        result.setFailedCount(0);
        result.setErrorDetails(new ArrayList<>());
        
        for (KnowledgeImportDTO dto : importList) {
            try {
                // 数据校验
                String validationError = validateKnowledge(dto);
                if (validationError != null) {
                    result.addError(dto.getRowNumber(), dto.getQuestion(), validationError);
                    continue;
                }
                
                // 检查是否已存在相同问题
                QueryWrapper<KnowledgeBase> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("question", dto.getQuestion());
                KnowledgeBase existing = knowledgeBaseMapper.selectOne(queryWrapper);
                
                if (existing != null) {
                    result.addError(dto.getRowNumber(), dto.getQuestion(), "问题已存在");
                    continue;
                }
                
                // 插入数据库
                KnowledgeBase knowledge = new KnowledgeBase();
                knowledge.setQuestion(dto.getQuestion());
                knowledge.setAnswer(dto.getAnswer());
                knowledge.setCategory(dto.getCategory());
                knowledge.setSource(dto.getSource());
                knowledge.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : 1);
                knowledge.setCreatedAt(LocalDateTime.now());
                knowledge.setUpdatedAt(LocalDateTime.now());
                
                knowledgeBaseMapper.insert(knowledge);
                result.setSuccessCount(result.getSuccessCount() + 1);
                
            } catch (Exception e) {
                log.error("导入第 {} 行失败: {}", dto.getRowNumber(), e.getMessage());
                result.addError(dto.getRowNumber(), dto.getQuestion(), "系统错误: " + e.getMessage());
            }
        }
        
        log.info("导入完成 - 总数: {}, 成功: {}, 失败: {}", 
                result.getTotalCount(), result.getSuccessCount(), result.getFailedCount());
        
        return result;
    }
    
    /**
     * 数据校验
     */
    private String validateKnowledge(KnowledgeImportDTO dto) {
        if (!StringUtils.hasText(dto.getQuestion())) {
            return "问题不能为空";
        }
        
        if (dto.getQuestion().length() > 500) {
            return "问题长度不能超过 500 字符";
        }
        
        if (!StringUtils.hasText(dto.getAnswer())) {
            return "答案不能为空";
        }
        
        if (dto.getAnswer().length() > 5000) {
            return "答案长度不能超过 5000 字符";
        }
        
        if (dto.getCategory() != null && dto.getCategory().length() > 50) {
            return "分类长度不能超过 50 字符";
        }
        
        if (dto.getSource() != null && dto.getSource().length() > 100) {
            return "来源长度不能超过 100 字符";
        }
        
        return null; // 校验通过
    }
    
    @Override
    public void exportToExcel(HttpServletResponse response, String category) throws IOException {
        // 查询数据
        QueryWrapper<KnowledgeBase> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(category)) {
            queryWrapper.eq("category", category);
        }
        queryWrapper.orderByDesc("created_at");
        List<KnowledgeBase> knowledgeList = knowledgeBaseMapper.selectList(queryWrapper);
        
        // 创建 Excel
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("知识库");
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);
            
            for (int i = 0; i < EXCEL_HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(EXCEL_HEADERS[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // 填充数据
            int rowNum = 1;
            for (KnowledgeBase knowledge : knowledgeList) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(knowledge.getQuestion());
                row.createCell(1).setCellValue(knowledge.getAnswer());
                row.createCell(2).setCellValue(knowledge.getCategory());
                row.createCell(3).setCellValue(knowledge.getSource());
                row.createCell(4).setCellValue(knowledge.getIsPublic() == 1 ? "公开" : "私有");
            }
            
            // 自动调整列宽
            for (int i = 0; i < EXCEL_HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            // 设置响应头
            String filename = "知识库导出_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
            
            // 写入响应流
            workbook.write(response.getOutputStream());
        }
        
        log.info("导出 Excel 完成，共 {} 条记录", knowledgeList.size());
    }
    
    @Override
    public void exportToCSV(HttpServletResponse response, String category) throws IOException {
        // 查询数据
        QueryWrapper<KnowledgeBase> queryWrapper = new QueryWrapper<>();
        if (StringUtils.hasText(category)) {
            queryWrapper.eq("category", category);
        }
        queryWrapper.orderByDesc("created_at");
        List<KnowledgeBase> knowledgeList = knowledgeBaseMapper.selectList(queryWrapper);
        
        // 设置响应头
        String filename = "知识库导出_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
        
        // 写入 BOM（Excel 识别 UTF-8）
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        
        // 写入 CSV
        try (CSVWriter csvWriter = new CSVWriter(new OutputStreamWriter(response.getOutputStream(), "UTF-8"))) {
            // 写入标题行
            csvWriter.writeNext(CSV_HEADERS);
            
            // 写入数据
            for (KnowledgeBase knowledge : knowledgeList) {
                String[] line = {
                    knowledge.getQuestion(),
                    knowledge.getAnswer(),
                    knowledge.getCategory(),
                    knowledge.getSource(),
                    knowledge.getIsPublic() == 1 ? "1" : "0"
                };
                csvWriter.writeNext(line);
            }
        }
        
        log.info("导出 CSV 完成，共 {} 条记录", knowledgeList.size());
    }
    
    // ==================== 辅助方法 ====================
    
    /**
     * 获取单元格值
     */
    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }
    
    /**
     * 解析是否公开
     */
    private Integer parseIsPublic(String value) {
        if (value == null || value.trim().isEmpty()) {
            return 1; // 默认公开
        }
        
        String trimmed = value.trim();
        if (trimmed.equals("1") || trimmed.equals("公开") || trimmed.equalsIgnoreCase("public")) {
            return 1;
        } else if (trimmed.equals("0") || trimmed.equals("私有") || trimmed.equalsIgnoreCase("private")) {
            return 0;
        }
        
        return 1; // 默认公开
    }
    
    /**
     * 创建表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }
}
