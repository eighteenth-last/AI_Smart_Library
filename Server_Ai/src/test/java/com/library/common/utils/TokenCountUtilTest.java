package com.library.common.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * TokenCountUtil 测试类
 */
class TokenCountUtilTest {

    @Test
    void testCountTokens_Chinese() {
        // 测试中文文本
        String chineseText = "你好，世界！";
        int tokens = TokenCountUtil.countTokens(chineseText);
        
        System.out.println("中文文本: " + chineseText);
        System.out.println("Token 数量: " + tokens);
        
        assertTrue(tokens > 0, "Token 数量应该大于 0");
    }

    @Test
    void testCountTokens_English() {
        // 测试英文文本
        String englishText = "Hello, World!";
        int tokens = TokenCountUtil.countTokens(englishText);
        
        System.out.println("英文文本: " + englishText);
        System.out.println("Token 数量: " + tokens);
        
        assertTrue(tokens > 0, "Token 数量应该大于 0");
    }

    @Test
    void testCountTokens_Mixed() {
        // 测试中英混合文本
        String mixedText = "推荐一本科幻小说，比如 The Three-Body Problem";
        int tokens = TokenCountUtil.countTokens(mixedText);
        
        System.out.println("混合文本: " + mixedText);
        System.out.println("Token 数量: " + tokens);
        
        assertTrue(tokens > 0, "Token 数量应该大于 0");
    }

    @Test
    void testCountTokens_LongText() {
        // 测试长文本
        String longText = "《三体》是刘慈欣创作的系列长篇科幻小说，由《三体》、《三体Ⅱ·黑暗森林》、《三体Ⅲ·死神永生》组成，" +
                "第一部于2006年5月起在《科幻世界》杂志上连载，第二部于2008年5月首次出版，第三部则于2010年11月出版。";
        int tokens = TokenCountUtil.countTokens(longText);
        
        System.out.println("长文本长度: " + longText.length() + " 字符");
        System.out.println("Token 数量: " + tokens);
        
        assertTrue(tokens > 0, "Token 数量应该大于 0");
        // 中文文本 Token 数可能大于字符数，因为每个中文字符可能被编码为多个 Token
        assertTrue(tokens > longText.length() * 0.5, "Token 数量应该大于字符数的 50%");
    }

    @Test
    void testCountTokens_EmptyString() {
        // 测试空字符串
        String emptyText = "";
        int tokens = TokenCountUtil.countTokens(emptyText);
        
        assertEquals(0, tokens, "空字符串的 Token 应该为 0");
    }

    @Test
    void testCountTokens_Null() {
        // 测试 null
        int tokens = TokenCountUtil.countTokens();
        
        assertEquals(0, tokens, "null 的 Token 应该为 0");
    }

    @Test
    void testCountTokens_MultipleTexts() {
        // 测试多个文本
        String text1 = "你好";
        String text2 = "世界";
        String text3 = "Hello World";
        
        int totalTokens = TokenCountUtil.countTokens(text1, text2, text3);
        int separateTokens = TokenCountUtil.countTokens(text1) + 
                            TokenCountUtil.countTokens(text2) + 
                            TokenCountUtil.countTokens(text3);
        
        System.out.println("批量计算 Token: " + totalTokens);
        System.out.println("分别计算 Token: " + separateTokens);
        
        assertEquals(separateTokens, totalTokens, "批量计算应该等于分别计算的总和");
    }

    @Test
    void testCountMessageTokens() {
        // 测试消息 Token 计算
        String role = "user";
        String content = "推荐一本科幻小说";
        
        int messageTokens = TokenCountUtil.countMessageTokens(role, content);
        int contentTokens = TokenCountUtil.countTokens(content);
        
        System.out.println("消息内容: " + content);
        System.out.println("内容 Token: " + contentTokens);
        System.out.println("消息 Token (含格式): " + messageTokens);
        
        assertTrue(messageTokens > contentTokens, "消息 Token 应该大于内容 Token（包含格式）");
    }

    @Test
    void testGetEncodingName() {
        // 测试获取编码器名称
        String encodingName = TokenCountUtil.getEncodingName();
        
        System.out.println("编码器名称: " + encodingName);
        
        assertNotNull(encodingName, "编码器名称不应为 null");
        assertEquals("cl100k_base", encodingName, "应该使用 cl100k_base 编码器");
    }

    @Test
    void testRagPromptTokens() {
        // 模拟 RAG Prompt 场景
        String question = "请推荐一本科幻小说";
        String context = "《三体》是刘慈欣创作的系列长篇科幻小说，获得雨果奖最佳长篇小说奖。";
        String prompt = String.format(
            "根据以下知识库内容回答问题：\n\n知识库：%s\n\n问题：%s\n\n请基于知识库内容给出详细回答。",
            context, question
        );
        
        int tokens = TokenCountUtil.countTokens(prompt);
        
        System.out.println("RAG Prompt:");
        System.out.println(prompt);
        System.out.println("\nToken 数量: " + tokens);
        
        assertTrue(tokens > 0, "RAG Prompt Token 应该大于 0");
    }
}
