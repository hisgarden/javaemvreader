/*
 * Copyright 2010 sasc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sasc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import sasc.iso7816.AIDTest;
import sasc.iso7816.BERTLVTest;
import sasc.emv.EMVSessionTest;
import sasc.emv.ApplicationPriorityIndicatorTest;
import sasc.emv.EMVUtilTest;
import sasc.util.ISO3166_1Test;

/**
 * Comprehensive Test Suite for EMV Reader
 * Includes all TDD tests for critical components
 * 
 * Run with: mvn test
 * 
 * @author sasc
 */
@RunWith(Suite.class)
@SuiteClasses({
    // ISO 7816 Tests
    AIDTest.class,
    BERTLVTest.class,
    
    // EMV Tests
    EMVSessionTest.class,
    ApplicationPriorityIndicatorTest.class,
    EMVUtilTest.class,
    
    // Card Emulation Tests
    CardEmulatorTest.class,
    
    // Utility Tests
    ISO3166_1Test.class
})
public class TestSuite {
    // This class is used only as a holder for the above annotations
} 