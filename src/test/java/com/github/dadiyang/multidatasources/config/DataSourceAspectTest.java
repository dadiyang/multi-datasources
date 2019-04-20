package com.github.dadiyang.multidatasources.config;

import com.github.dadiyang.multidatasources.annotation.Table;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 对多数据源切面方法进行参数化单元测试
 *
 * @author dadiyang
 * @since 2019/4/19
 */
@RunWith(Parameterized.class)
public class DataSourceAspectTest {
    private Object[] args;
    private Object[] expectedProceedArgs;
    private Method testClassMethod;
    private String expectedDataSourceKey;
    private static final int TABLE_COUNT = 32;
    private static final int TABLE_PER_DB = 16;

    public DataSourceAspectTest(Object[] args, Object[] expectedProceedArgs, Method testClassMethod, String expectedDataSourceKey) {
        this.args = args;
        this.expectedProceedArgs = expectedProceedArgs;
        this.testClassMethod = testClassMethod;
        this.expectedDataSourceKey = expectedDataSourceKey;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws NoSuchMethodException {
        List<Object[]> params = new LinkedList<>();
        // 使用方法参数做数据源选择依据
        for (int i = 0; i < TABLE_COUNT; i++) {
            params.add(new Object[]{
                    new Object[]{i},
                    new Object[]{i % TABLE_COUNT},
                    TestClazz.class.getMethod("testTableParameter", int.class),
                    "ds-" + i / TABLE_COUNT
            });
        }
        // 使用对象中的指定字段做数据源选择依据
        for (int i = 0; i < TABLE_COUNT; i++) {
            params.add(new Object[]{
                    new Object[]{new TestTableFieldClass(i)},
                    new Object[]{new TestTableFieldClass(i % TABLE_COUNT)},
                    TestClazz.class.getMethod("testTableField", TestTableFieldClass.class),
                    "ds-" + i / TABLE_COUNT
            });
        }
        // 没有参数指定分库分表依据，此时 key 应该为 null
        for (int i = 0; i < TABLE_COUNT; i++) {
            params.add(new Object[]{
                    new Object[]{},
                    new Object[]{},
                    TestClazz.class.getMethod("testDefaultSource"),
                    null
            });
        }
        return params;
    }

    @Test
    public void around() throws Throwable {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        DataSourceAspect aspect = new DataSourceAspect();
        // 设置每个库中表的数量
        aspect.setTablePerDb(TABLE_PER_DB);
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        // 设置被测试的方法
        when(signature.getMethod()).thenReturn(testClassMethod);
        when(joinPoint.getSignature()).thenReturn(signature);
        // 被测试方法的参数
        when(joinPoint.getArgs()).thenReturn(args);
        // 执行连接点的 proceed 方法，在执行过程中，可能获取到当前线程的数据源 key
        when(joinPoint.proceed(expectedProceedArgs)).thenAnswer(invocation -> {
            assertEquals("判定的数据源", expectedDataSourceKey, routingDataSource.determineCurrentLookupKey());
            return "OK";
        });
        // 触发被测方法
        aspect.around(joinPoint);
        // 切面方法结束后应移除数据源key
        assertNull("切面方法结束后应移除数据源key", routingDataSource.determineCurrentLookupKey());
    }

    interface TestClazz {
        String testTableParameter(@Table int table);

        String testTableField(TestTableFieldClass testTableFieldClass);

        String testDefaultSource();
    }

    static class TestTableFieldClass {
        @Table
        private int table;

        public TestTableFieldClass(int table) {
            this.table = table;
        }
    }
}