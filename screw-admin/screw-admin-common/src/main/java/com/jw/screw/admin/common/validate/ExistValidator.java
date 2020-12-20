package com.jw.screw.admin.common.validate;

import com.jw.screw.admin.common.constant.StringPool;
import com.jw.screw.admin.common.exception.BasicOperationException;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 用于数据库字段唯一性的判断
 * @author jiangw
 * @date 2020/11/18 17:49
 * @since 1.0
 */
class ExistValidator implements Validator{

    private static Pattern humpPattern = Pattern.compile("[A-Z]");

    /**
     * 匹配错误信息中，如果有${value}则取当前字段
     */
    private final static String VALUE_MACHINE = "${value}";

    private final Lock lock;

    private final Existing existing;

    public ExistValidator(Existing existing) {
        this.existing = existing;
        this.lock = new ReentrantLock();
    }


    @Override
    public ValidationResult validate() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, BasicOperationException {
        Assert.notNull(existing, "数据为空，无法进行校验");
        lock.lock();
        try {
            ExistMapper mapper = existing.getExistMapper();
            Assert.notNull(mapper, "校验mapper为空");
            Object entity = existing.getEntity();
            List<ExistField> filedValues = parseEntity(entity);
            // 如果实体对象没有标识的字段，或者标识的字段为空，那么不继续验证，直接返回true
            if (CollectionUtils.isEmpty(filedValues)) {
                return new ValidationResult(true, "");
            }
            // 获取当前构造的实体是否存在id
            List<ExistField> idFields = filedValues.stream().filter(field -> field.id).collect(Collectors.toList());
            ExistField idField = null;
            if (!CollectionUtils.isEmpty(idFields) && idFields.size() == 1) {
                idField = idFields.get(0);
            }
            // 获取当前构造的实体是否存在extension
            List<ExistField> extensions = filedValues.stream().filter(filed -> filed.extension).collect(Collectors.toList());
            for (ExistField filedValue : filedValues) {
                // 排除属于id，避免重复判断出错
                if (filedValue == idField) {
                    continue;
                }
                // 排除数据extension字段，避免重复出错
                if (extensions.contains(filedValue)) {
                    continue;
                }
                // map的每一个槽位一定有值
                StringBuilder whereSql = new StringBuilder();
                appendSql(whereSql, filedValue);
                // 拼接id
                if (!ObjectUtils.isEmpty(idField)) {
                    appendSql(whereSql, idField);
                }
                // 拼接extension
                if (!CollectionUtils.isEmpty(extensions)) {
                    for (ExistField extension : extensions) {
                        appendSql(whereSql, extension);
                    }
                }
                // 判断当前字段是否有id的字段，若有则拼接上当前字段
                int exist = mapper.isExist(whereSql.toString());
                // 如果其中一个存在值校验不成功，那么就结束整个校验
                if (exist > 0) {
                    return new ValidationResult(false, filedValue.getErrorMessage());
                }
            }
        } finally {
            lock.unlock();
        }
        return new ValidationResult(true, "");
    }

    /**
     * 解析实体，实体的字段上是否存在有@unique标识，若存在获取该字段的值，所以实体对象必须实现get方法
     * @param entity 实体对象，对象必须实现get方法
     * @return 数据库字段名称与value的map集合
     */
    private List<ExistField> parseEntity(Object entity) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, BasicOperationException {
        List<ExistField> filedValues = new ArrayList<>();
        Class<?> entityClass = entity.getClass();
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            Exist exist = field.getAnnotation(Exist.class);
            if (ObjectUtils.isEmpty(exist)) {
                continue;
            }
            // 数据库字段名
            String dataBaseFiled;
            // 标识的字段名
            String unique = exist.unique();
            boolean extension = exist.extension();
            boolean isId = exist.id();
            // 实体字段名
            String filedName = field.getName();
            if (StringUtils.isEmpty(unique)) {
                // 当没有标识时，取当前实体的名称，转换成数据库字段标准格式
                dataBaseFiled = humpToLine(filedName);
            } else {
                // 解析字段名，确认其是否是标准的数据库字段格式
                if(!specification(unique)) {
                    dataBaseFiled = humpToLine(unique);
                } else {
                    dataBaseFiled = unique;
                }
            }
            // 调用当前字段的get方法，获取值，如果没用值那么就不加入数据库的判断
            String invokeName = filedName.substring(0, 1).toUpperCase() + filedName.substring(1);
            Method method = entityClass.getMethod(StringPool.GET + invokeName, null);
            Object value = method.invoke(entity, null);
            if (!ObjectUtils.isEmpty(value)) {
                String errorMessage = exist.errorMessage();
                String rule = exist.rule();
                if (isId) {
                    // 检查规则是否是 '!='
                    if (!StringPool.NON_EQUALS.equals(rule)) {
                        rule = StringPool.NON_EQUALS;
                    }
                }
                if (extension) {
                    // 检查规则是否是 '='
                    if (!StringPool.EQUALS.equals(rule)) {
                        rule = StringPool.EQUALS;
                    }
                }
                // 判断错误信息是否需要取当前的value
                if (errorMessage.contains(VALUE_MACHINE)) {
                    errorMessage = errorMessage.replace(VALUE_MACHINE, value.toString());
                }
                filedValues.add(new ExistField(dataBaseFiled, value, rule, isId, extension, errorMessage));
            } else {
                if (!isId && !extension) {
                    throw new BasicOperationException("值为空，无法进行判断");
                }
            }
        }
        return filedValues;
    }


    private boolean specification(String filed) {
        return filed.contains(StringPool.UNDERSCORE);
    }

    private String humpToLine(String filed) {
        Matcher matcher = humpPattern.matcher(filed);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString().toUpperCase();
    }

    private void appendSql(StringBuilder whereSql, ExistField field) {
        whereSql.append(" ").append(StringPool.AND).append(" ")
                .append(field.getDataBaseFiled())
                .append(field.getRule())
                .append(getValue(field.getValue()));
    }

    private String getValue(Object value) {
        if (value instanceof String) {
            return StringPool.SINGLE_QUOTE + value.toString() + StringPool.SINGLE_QUOTE;
        }
        return value.toString();
    }
}
