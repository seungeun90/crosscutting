# Crosscutting: AOP & Interceptor를 활용한 다중 스키마 및 컬럼 암·복호화 예제

## 개요
이 프로젝트는 Interceptor와 AOP를 사용해 아래 조건을 구현한 예제입니다. 
- DB 스키마 동적 변경 
- DB 특정 컬럼 암복호화 공통화

## 주요 기능
### Interceptor 기반 고객사 식별
```
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            CustomAuthenticationToken authentication = (CustomAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            if(!authentication.isSystemAdmin()){
                if(authentication.getTenant() == null ||
                        !StringUtils.hasText(authentication.getTenant().code())){
                    throw new RuntimeException("고객 식별에 실패하였습니다.");
                }
                String code = authentication.getTenant().code();
                TenantContextHolder.setTenant(code);
            }
        } catch (Exception e) {
            log.error("auth {}" ,e);
            throw e;
        }
        return true;
    }
```

### Custom Annotation과 AOP 를 이용한 고객사 코드 추출
- 시스템 어드민이 접근할 수 있는 테넌트 관리자 정보 요청 메서드의 Custom Annotation 확인
```
    @SetTenant
    public UserDao getUser(){
        return null;
    }
```
```
@Aspect
public class TenantAspect {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String EXTRACT_FILED_TARGET = "tenant";

    @Around("@annotation(com.clp.admin.common.tenant.SetTenant) || @within(com.clp.admin.common.tenant.SetTenant)")
    public Object setTenant(ProceedingJoinPoint joinPoint) throws Throwable {
        String currentTenant = TenantContextHolder.getTenant();
        
        if (currentTenant != null) {
            log.info("Tenant already set: {}", currentTenant);
            return joinPoint.proceed(); 
        }
        
        for (Object arg : joinPoint.getArgs()) {
            String clientCode = extractTenantCode(arg);
            if (clientCode != null) {
                TenantContextHolder.setTenant(clientCode);
                break;
            }
        }
        
        if(TenantContextHolder.getTenant()==null) {
            throw new RuntimeException("Tenant 정보가 누락되었습니다.");
        }
        
        try {
            return joinPoint.proceed();
        } finally {
            TenantContextHolder.clear();
            log.info("Tenant cleared====================");
        }
    }
```
### DB 스키마 동적 변경 
- @Table(schema= Schema.SYSTEM) 전체 사용자가 접근가능한 레파지토리 분류 Custom Annotation 검사
```
@Repository
@Table(schema= Schema.SYSTEM)
public interface RegionRepository {
    void getRegions();
}
```
```
Intercepts({
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
@Component
@RequiredArgsConstructor
public class SchemaSettingInterceptor implements Interceptor {
    private final DatabaseProperties dbProperties;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        /**
         * 현재 테넌트 확인
         * */
        String tenant = TenantContextHolder.getTenant();
        /**
         * @Table 식별을 통해 전역 테이블 접근 여부 확인
         * */
        String queryID = ((MappedStatement) invocation.getArgs()[0]).getId();
        String className = queryID.substring(0, queryID.lastIndexOf("."));
        Class<?> mapperClass = Class.forName(className);
        try {
            mapperClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        
        if (mapperClass.isAnnotationPresent(Table.class)) {
            Table tableAnnotation = mapperClass.getAnnotation(Table.class);
            if(Schema.SYSTEM.equals(tableAnnotation.schema())) {
                tenant = dbProperties.getDefaultSchema();
            }
        }
       /**
        * schema 변경 실행
        * */
        try {
            Connection connection = ((Executor) invocation.getTarget()).getTransaction().getConnection();
            String cmd = dbProperties.getSchemaCmd().replace("${tenant}", tenant);
            try (Statement statement = connection.createStatement()) {
                statement.execute(cmd);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing schema command for tenant: " + tenant, e);
        }

        try {
            return invocation.proceed();
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
```

### AOP를 활용한 특정 컬럼 암·복호화
- `@Secured` 어노테이션이 붙은 필드 자동 암·복호화
```
@Getter @Setter
@NoArgsConstructor
public class UserDao {
    private String id;

    @Secured
    private String email;
}
```

```
@Aspect
public class SecureDataAspect {

    private AESUtil aesUtil;

    /**
     *  aspectj 소스에 spring bean 주입
     */
    public SecureDataAspect(){
        this.aesUtil = ApplicationContextProvider.getApplicationContext().getBean(AESUtil.class);
    }

    /**
     * 암호화된 개인정보 read 할 때 복호화
     * 조건 :
     * 1. repository/adapter/ 하위 클래스에서 호출 시
     * 2. Dao getter 호출 시
     */
    @Around("call(* com.example.crosscutting.demo.repository.dao.*.get*(..)) && packageInRepositoryAdapter()" )
    public Object decrypt(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String fieldName = extractFieldName(method.getName());
        Field field = method.getDeclaringClass().getDeclaredField(fieldName);

        // 필드에 @Secured 어노테이션이 있는지 확인
        if (field.isAnnotationPresent(Secured.class)) {
            // Getter 호출
            Object result = joinPoint.proceed();
            //복호화
            if (result instanceof String) {
                String decryptedValue = aesUtil.decrypt((String) result); // 복호화 로직
                log.info("Aspect Secured Decrypt field= {}, original= {}, decrypted= {}", field.getName(), result, decryptedValue);
                return decryptedValue; // 복호화된 값 반환
            }
        }

        return joinPoint.proceed();
    }
```

## 기술 스택
- **Spring Boot**
- **MyBatis (iBatis)**
- **AOP (Aspect-Oriented Programming)**
- **Interceptor (Spring & MyBatis)**
