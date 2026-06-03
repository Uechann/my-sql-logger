# 01. 프로젝트 세팅

## 목표
`java-library` + `maven-publish` 플러그인으로 라이브러리 프로젝트를 구성한다.

## build.gradle 핵심 포인트

```groovy
plugins {
    id 'java-library'   // api / compileOnly 의존성 구분 가능
    id 'maven-publish'  // ./gradlew publishToMavenLocal 배포 가능
}
```

### 의존성 스코프 구분

| 스코프 | 의미 |
|---|---|
| `compileOnly` | 라이브러리를 사용하는 프로젝트가 이미 가지고 있는 것. JAR에 포함하지 않음 |
| `annotationProcessor` | 컴파일 타임에만 필요 (`spring-boot-configuration-processor`로 IDE 자동완성 생성) |
| `testImplementation` | 테스트에서만 필요 |

`spring-boot-autoconfigure`와 `slf4j-api`를 `compileOnly`로 선언한 이유:
사용자의 Spring Boot 프로젝트에 이미 이 라이브러리들이 있다. 내 JAR에 함께 넣으면 버전 충돌이 생길 수 있다.

### Gradle Wrapper

`gradlew`가 없는 초기 상태에서는 Gradle이 설치되지 않은 환경도 빌드 가능하도록 wrapper를 설정해야 한다.

```
gradle/wrapper/gradle-wrapper.properties  ← 어떤 Gradle 버전을 받을지 지정
gradle/wrapper/gradle-wrapper.jar         ← wrapper 실행 JAR
gradlew                                    ← Unix용 실행 스크립트
```

### useJUnitPlatform() 필수

Gradle 기본 설정은 JUnit 4이다. JUnit 5(Jupiter)를 사용하려면 반드시 추가해야 한다:

```groovy
test {
    useJUnitPlatform()
}
```

이 설정이 없으면 테스트 클래스가 컴파일은 되지만 하나도 실행되지 않는다 (0 tests, BUILD SUCCESSFUL).
