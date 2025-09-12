package com.spring.fit.backend.user.service;

import com.spring.fit.backend.user.service.impl.RecentViewServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecentViewServiceTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    @InjectMocks
    private RecentViewServiceImpl recentViewService;

    private final long userId = 123L;
    private final long productId1 = 1L;
    private final long productId2 = 2L;
    private final long productId3 = 3L;
    private final long productId4 = 4L;

    @BeforeEach
    void setUp() {
        when(redis.opsForZSet()).thenReturn(zSetOperations);
    }

    @Test
    void testAddViewed_ShouldAddProductToRedis() {
        // Given
        when(zSetOperations.zCard(anyString())).thenReturn(1L);

        // When
        recentViewService.addViewed(userId, productId1);

        // Then
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Double> scoreCaptor = ArgumentCaptor.forClass(Double.class);
        
        verify(zSetOperations).add(keyCaptor.capture(), valueCaptor.capture(), scoreCaptor.capture());
        verify(redis).expire(anyString(), eq(Duration.ofDays(30)));
        
        assertEquals("recent:prod:123", keyCaptor.getValue());
        assertEquals("1", valueCaptor.getValue());
        assertNotNull(scoreCaptor.getValue());
        assertTrue(scoreCaptor.getValue() > 0);
    }

    @Test
    void testAddViewed_ShouldRemoveOldestWhenExceedLimit() {
        // Given - đã có 4 phần tử (vượt quá limit = 3)
        when(zSetOperations.zCard(anyString())).thenReturn(4L);

        // When
        recentViewService.addViewed(userId, productId4);

        // Then
        verify(zSetOperations).add(anyString(), eq("4"), anyDouble());
        verify(zSetOperations).removeRange(anyString(), eq(0L), eq(0L)); // Xóa 1 phần tử cũ nhất
        verify(redis).expire(anyString(), eq(Duration.ofDays(30)));
    }

    @Test
    void testGetRecentIds_ShouldReturnEmptyListWhenNoData() {
        // Given
        when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong())).thenReturn(null);

        // When
        List<Long> result = recentViewService.getRecentIds(userId);

        // Then
        assertTrue(result.isEmpty());
        verify(zSetOperations).reverseRange("recent:prod:123", 0, 2); // LIMIT - 1 = 2
    }

    @Test
    void testGetRecentIds_ShouldReturnProductIdsInCorrectOrder() {
        // Given - Set với thứ tự từ mới nhất đến cũ nhất
        Set<String> mockData = new LinkedHashSet<>();
        mockData.add("3"); // Mới nhất
        mockData.add("1"); 
        mockData.add("2"); // Cũ nhất
        
        when(zSetOperations.reverseRange(anyString(), anyLong(), anyLong())).thenReturn(mockData);

        // When
        List<Long> result = recentViewService.getRecentIds(userId);

        // Then
        assertEquals(3, result.size());
        assertEquals(List.of(3L, 1L, 2L), result);
        verify(zSetOperations).reverseRange("recent:prod:123", 0, 2);
    }

    @Test
    void testRemoveSelected_ShouldRemoveMultipleProducts() {
        // Given
        List<Long> productsToRemove = Arrays.asList(productId1, productId2, productId3);

        // When
        recentViewService.removeSelected(userId, productsToRemove);

        // Then
        ArgumentCaptor<Object[]> membersCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(zSetOperations).remove(eq("recent:prod:123"), membersCaptor.capture());
        
        Object[] capturedMembers = membersCaptor.getValue();
        assertEquals(3, capturedMembers.length);
        assertEquals("1", capturedMembers[0]);
        assertEquals("2", capturedMembers[1]);
        assertEquals("3", capturedMembers[2]);
    }

    @Test
    void testRemoveSelected_ShouldFilterOutNullValues() {
        // Given
        List<Long> productsWithNull = Arrays.asList(productId1, null, productId2);

        // When
        recentViewService.removeSelected(userId, productsWithNull);

        // Then
        ArgumentCaptor<Object[]> membersCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(zSetOperations).remove(eq("recent:prod:123"), membersCaptor.capture());
        
        Object[] capturedMembers = membersCaptor.getValue();
        assertEquals(2, capturedMembers.length);
        assertEquals("1", capturedMembers[0]);
        assertEquals("2", capturedMembers[1]);
    }

    @Test
    void testKeyGeneration_ShouldGenerateCorrectKey() {
        // Test thông qua việc verify các method calls
        recentViewService.addViewed(456L, productId1);
        
        verify(zSetOperations).add(eq("recent:prod:456"), anyString(), anyDouble());
    }

    @Test
    void testAddViewed_ShouldUpdateScoreWhenProductAlreadyExists() {
        // Given
        when(zSetOperations.zCard(anyString())).thenReturn(2L);

        // When - Thêm cùng một product 2 lần
        recentViewService.addViewed(userId, productId1);
        recentViewService.addViewed(userId, productId1);

        // Then - Chỉ verify rằng add được gọi 2 lần (Redis sẽ tự động update score)
        verify(zSetOperations, times(2)).add(anyString(), eq("1"), anyDouble());
        verify(redis, times(2)).expire(anyString(), eq(Duration.ofDays(30)));
        
        // Không có removeRange vì chưa vượt quá limit
        verify(zSetOperations, never()).removeRange(anyString(), anyLong(), anyLong());
    }
}