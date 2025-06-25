# Java EMV Reader 💳

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/hisgarden/javaemvreader)
[![Test Coverage](https://img.shields.io/badge/tests-92%25%20passing-brightgreen.svg)](https://github.com/hisgarden/javaemvreader)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE-2_0.txt)
[![Java Version](https://img.shields.io/badge/java-11%2B-orange.svg)](https://openjdk.org/)

A comprehensive Java library for reading and processing **EMV (Europay, Mastercard, Visa)** payment cards, supporting both contact and contactless transactions. Built with **Test-Driven Development (TDD)** principles and enterprise-level testing standards.

## 🚀 Features

### 🏗️ **Core EMV Functionality**
- **ISO 7816-4 Compliance** - Full Application Identifier (AID) validation
- **EMV Transaction Processing** - Complete payment card transaction flow
- **BER-TLV Data Parsing** - Robust EMV data structure handling
- **Card Emulation** - Software-based card emulation for testing
- **Multi-Application Support** - Handle multiple payment applications per card

### 🔧 **Smart Card Support**
- **Contact Cards** - Traditional chip card interface
- **Contactless Cards** - NFC/RFID payment cards
- **PSE/PPSE Discovery** - Payment System Environment detection
- **Application Selection** - Automatic and manual app selection
- **PIN Verification** - Secure PIN handling and validation

### 🛡️ **Security & Cryptography**
- **Static Data Authentication (SDA)** - Card authenticity verification
- **Certificate Chain Validation** - CA and issuer certificate processing
- **Cryptogram Verification** - Transaction cryptogram validation
- **Risk Management** - Terminal and issuer risk assessment

## 📊 **Test-Driven Development (TDD)**

This project follows **strict TDD principles** with comprehensive test coverage:

### 🧪 **Test Suite Statistics**
- **123 Total Tests** across 8 test classes
- **92% Success Rate** (113 passing tests)
- **Modern Testing Framework** (JUnit 4.13.2, Mockito 3.12.4, AssertJ 3.21.0)

| Component | Tests | Success Rate | Coverage |
|-----------|-------|--------------|----------|
| **AID Validation** | 10/10 | 100% ✅ | ISO 7816-4 compliance |
| **BER-TLV Parsing** | 14/15 | 93% ✅ | EMV data structures |
| **Card Emulation** | 20/20 | 100% ✅ | Complete functionality |
| **EMV Session** | 9/15 | 60% 🔄 | Transaction flow |
| **Utilities** | 60/63 | 95% ✅ | Helper functions |

### 🎯 **TDD Test Classes**
- `AIDTest.java` - Application Identifier validation
- `BERTLVTest.java` - EMV data structure parsing
- `CardEmulatorTest.java` - Card emulation testing
- `EMVSessionTest.java` - Transaction flow with Mockito
- `EMVUtilTest.java` - Utility function validation

## 🏃‍♂️ **Quick Start**

### Prerequisites
- **Java 11+** (OpenJDK recommended)
- **Maven 3.6+** for dependency management
- **Smart card reader** (optional, for physical cards)

### Installation

```bash
# Clone the repository
git clone https://github.com/hisgarden/javaemvreader.git
cd javaemvreader

# Build the project
mvn clean compile

# Run tests
mvn test

# Create JAR
mvn package
```

### Basic Usage

```java
import sasc.emv.*;
import sasc.terminal.*;

// Initialize EMV session
SmartCard card = new SmartCard();
CardConnection terminal = new SmartcardioCardConnection();
EMVSession session = new EMVSession(card, terminal);

// Discover and select applications
session.initContext();
List<EMVApplication> apps = card.getEmvApplications();

// Process transaction
for (EMVApplication app : apps) {
    session.selectApplication(app);
    session.initiateApplicationProcessing();
    session.prepareTransactionProcessing();
    session.performTransaction();
}
```

### Card Emulation

```java
import sasc.CardEmulator;

// Use built-in card emulation for testing
CardEmulator emulator = new CardEmulator("/sdacardtransaction.xml");
byte[] atr = emulator.getATR();

// Send APDU commands
byte[] selectPSE = EMVAPDUCommands.selectPSE();
CardResponse response = emulator.transmit(selectPSE);
```

## 📚 **Documentation**

### 📖 **Comprehensive Guides**
- **[RUN_TESTS.md](RUN_TESTS.md)** - Complete test execution guide
- **[TDD_IMPROVEMENTS.md](TDD_IMPROVEMENTS.md)** - Detailed TDD implementation
- **[TEST_EXECUTION_REPORT.md](TEST_EXECUTION_REPORT.md)** - Test analysis and results

### 🔧 **API Documentation**
- **EMVSession** - Main transaction processing class
- **SmartCard** - Card abstraction layer
- **CardEmulator** - Software card emulation
- **EMVUtil** - Utility functions for EMV processing
- **BERTLV** - BER-TLV data structure handling

## 🛠️ **Development**

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AIDTest

# Run with verbose output
mvn test -X

# Generate test reports
mvn test && open target/surefire-reports/index.html
```

### Test Categories

```bash
# Critical EMV components
mvn test -Dtest=AIDTest,BERTLVTest,CardEmulatorTest

# Transaction flow
mvn test -Dtest=EMVSessionTest

# Utility functions
mvn test -Dtest=EMVUtilTest,ApplicationPriorityIndicatorTest
```

### Building

```bash
# Clean build
mvn clean compile

# Create JAR with dependencies
mvn package assembly:single

# Install to local repository
mvn install
```

## 📁 **Project Structure**

```
javaemvreader/
├── src/main/java/sasc/           # Core EMV implementation
│   ├── emv/                      # EMV transaction processing
│   ├── iso7816/                  # ISO 7816-4 standard implementation
│   ├── smartcard/                # Smart card abstraction
│   └── terminal/                 # Terminal communication
├── src/test/java/sasc/           # Comprehensive test suite
│   ├── emv/                      # EMV transaction tests
│   └── iso7816/                  # ISO standard tests
├── src/main/resources/           # Configuration and data files
├── testkeys/                     # Test certificates and keys
└── docs/                         # Documentation
```

## 🤝 **Contributing**

We welcome contributions! Please follow our TDD approach:

1. **Fork** the repository
2. **Write failing tests** first (Red phase)
3. **Implement minimal code** to pass tests (Green phase)
4. **Refactor** and improve (Refactor phase)
5. **Submit pull request** with test coverage

### Development Guidelines
- Follow **TDD principles** (Red-Green-Refactor)
- Maintain **test coverage above 90%**
- Use **modern testing frameworks** (JUnit, Mockito, AssertJ)
- Include **comprehensive documentation**
- Follow **EMV specifications** and **ISO 7816-4 standards**

## 📄 **License**

This project is licensed under the **Apache License 2.0** - see the [LICENSE-2_0.txt](LICENSE-2_0.txt) file for details.

## 🔗 **Related Projects**

- **[EMV Specifications](https://www.emvco.com/)** - Official EMV standards
- **[ISO 7816](https://www.iso.org/standard/54550.html)** - Smart card standards
- **[PC/SC](https://pcscworkgroup.com/)** - Smart card reader interface

## 📞 **Support**

- **Issues**: [GitHub Issues](https://github.com/hisgarden/javaemvreader/issues)
- **Documentation**: See `docs/` directory
- **Email**: jin.wen@hisgarden.org

## 🏆 **Acknowledgments**

- **EMV Consortium** for payment card standards
- **SASC Community** for smart card development
- **TDD Community** for testing best practices
- **Contributors** who helped improve this project

---

**Built with ❤️ using Test-Driven Development principles** 