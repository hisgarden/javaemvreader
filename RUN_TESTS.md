# Running TDD Tests for Java EMV Reader

## Prerequisites Setup

### Install Java and Maven (macOS)

```bash
# Install Homebrew if not already installed
/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"

# Install Java JDK
brew install openjdk@11

# Add Java to PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@11/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Install Maven
brew install maven

# Verify installations
java -version
mvn -version
```

### Alternative: Download and Install Manually

1. **Java JDK**: Download from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://openjdk.org/)
2. **Maven**: Download from [Apache Maven](https://maven.apache.org/download.cgi)

## Running Tests

### Run All Tests
```bash
cd javaemvreader
mvn test
```

### Run Specific Test Classes
```bash
# Run AID tests (Application Identifier)
mvn test -Dtest=AIDTest

# Run BER-TLV tests (EMV data parsing)
mvn test -Dtest=BERTLVTest

# Run EMV Session tests (transaction flow)
mvn test -Dtest=EMVSessionTest

# Run Card Emulator tests
mvn test -Dtest=CardEmulatorTest

# Run all TDD tests via test suite
mvn test -Dtest=TestSuite
```

### Run Tests with Detailed Output
```bash
# Verbose output
mvn test -X

# Show all test results
mvn test -Dsurefire.useFile=false

# Generate test reports
mvn test
# Then open: target/surefire-reports/index.html
```

## Test Categories

### 🏗️ **Critical EMV Component Tests**

#### AIDTest.java (10 tests)
- ✅ `shouldCreateAIDFromHexString()` - AID creation from hex
- ✅ `shouldCreateAIDFromByteArray()` - AID creation from bytes  
- ✅ `shouldValidateAIDLength()` - ISO 7816-4 length compliance
- ✅ `shouldExtractRIDFromAID()` - RID extraction
- ✅ `shouldDetectPartialMatch()` - Application selection logic
- ✅ `shouldNotMatchDifferentAIDs()` - Negative matching
- ✅ `shouldImplementEqualsAndHashCode()` - Object contract
- ✅ `shouldProvideStringRepresentation()` - toString method
- ✅ `shouldHandleNullInput()` - Null safety
- ✅ `shouldHandleEmptyInput()` - Empty input validation

#### BERTLVTest.java (15 tests)  
- ✅ `shouldParseSimpleTLV()` - Basic TLV parsing
- ✅ `shouldParseComplexTLV()` - Complex nested structures
- ✅ `shouldParseNestedTLVStructure()` - Multi-level nesting
- ✅ `shouldHandleShortFormLength()` - Length encoding (0-127)
- ✅ `shouldHandleLongFormLength()` - Length encoding (>127)
- ✅ `shouldDetectConstructedTags()` - Tag type detection
- ✅ `shouldDetectPrimitiveTags()` - Primitive tag handling
- ✅ `shouldHandleMultibyteTag()` - Multi-byte tag support
- ✅ `shouldFindChildByTag()` - Child tag navigation
- ✅ `shouldHandleEmptyValue()` - Zero-length values
- ✅ `shouldThrowExceptionOnInvalidData()` - Error handling
- ✅ `shouldHandleZeroLengthStream()` - Edge cases
- ✅ `shouldProvideHumanReadableString()` - String representation
- ✅ `shouldCalculateCorrectBytesLength()` - Length calculation
- ✅ `shouldSerializeBackToBytes()` - Serialization

#### EMVSessionTest.java (15 tests)
- ✅ `shouldCreateSessionWithValidParameters()` - Session creation
- ✅ `shouldThrowExceptionForNullCard()` - Null validation
- ✅ `shouldThrowExceptionForNullTerminal()` - Parameter validation
- ✅ `shouldInitializeContextSuccessfully()` - PSE discovery
- ✅ `shouldHandlePSENotAvailable()` - PSE fallback to PPSE
- ✅ `shouldSelectApplicationSuccessfully()` - App selection
- ✅ `shouldThrowExceptionForApplicationSelectionFailure()` - Error handling
- ✅ `shouldInitiateApplicationProcessing()` - GPO processing
- ✅ `shouldThrowExceptionWhenNoApplicationSelected()` - State validation
- ✅ `shouldPrepareTransactionProcessing()` - Transaction prep
- ✅ `shouldPerformTransaction()` - Transaction execution
- ✅ `shouldHandleContextInitializationOnlyOnce()` - State management
- ✅ `shouldProvideAccessToCard()` - Getter methods
- ✅ `shouldHandleMultipleApplications()` - Multi-app support
- ✅ `shouldValidateApplicationStateTransitions()` - State machine

#### CardEmulatorTest.java (20 tests)
- ✅ `shouldCreateEmulatorWithValidCardFile()` - Emulator creation
- ✅ `shouldThrowExceptionForInvalidCardFile()` - File validation
- ✅ `shouldProvideValidATR()` - ATR provision
- ✅ `shouldHandleSelectPSECommand()` - PSE command
- ✅ `shouldHandleSelectPPSECommand()` - PPSE command
- ✅ `shouldHandleSelectApplicationCommand()` - App selection
- ✅ `shouldHandleGetProcessingOptionsCommand()` - GPO command
- ✅ `shouldHandleReadRecordCommand()` - Record reading
- ✅ `shouldHandleGetDataCommand()` - Data retrieval
- ✅ `shouldHandleVerifyPINCommand()` - PIN verification
- ✅ `shouldHandleInvalidCommand()` - Error handling
- ✅ `shouldHandleCommandWithIncorrectLength()` - Length validation
- ✅ `shouldProvideConnectionInformation()` - Connection info
- ✅ `shouldSupportDisconnect()` - Disconnection
- ✅ `shouldReturnNullTerminal()` - Terminal reference
- ✅ `shouldHandleMultipleCommandSequence()` - Command sequences
- ✅ `shouldMaintainStateBetweenCommands()` - State persistence
- ✅ `shouldHandleControlCommands()` - Control commands
- ✅ `shouldHandleCardReset()` - Reset handling
- ✅ `shouldReturnNullProtocol()` - Protocol info

## Expected Test Results

When properly configured and run, you should see:

```
[INFO] Tests run: 78, Failures: 0, Errors: 0, Skipped: 0

[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

## Troubleshooting

### Common Issues

1. **Java Not Found**: Ensure Java is in PATH
2. **Maven Not Found**: Install Maven and add to PATH  
3. **Dependency Issues**: Run `mvn clean install`
4. **Test Failures**: Check logs in `target/surefire-reports/`

### Test Dependencies Required

The tests require these dependencies (already added to pom.xml):
- JUnit 4.13.2
- Mockito 3.12.4  
- AssertJ 3.21.0
- Hamcrest 1.3

## Test Coverage Report

To generate coverage reports:
```bash
mvn test jacoco:report
# Open: target/site/jacoco/index.html
```

## Continuous Integration

These tests are designed to run in CI/CD pipelines:
```yaml
# GitHub Actions example
- name: Run Tests
  run: mvn test
  
- name: Upload Test Results
  uses: actions/upload-artifact@v2
  with:
    name: test-results
    path: target/surefire-reports/
``` 