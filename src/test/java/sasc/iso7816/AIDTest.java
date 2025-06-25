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
package sasc.iso7816;

import org.junit.Test;
import org.junit.Before;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.*;
import sasc.util.Util;

/**
 * Test Driven Development tests for AID (Application Identifier)
 * Following Red-Green-Refactor cycle
 * 
 * @author sasc
 */
public class AIDTest {
    
    private AID visaAID;
    private AID mastercardAID;
    
    @Before
    public void setUp() {
        // Common test data setup
        visaAID = new AID("A0 00 00 00 03 10 10");
        mastercardAID = new AID("A0 00 00 00 04 10 10");
    }
    
    @Test
    public void shouldCreateAIDFromHexString() {
        // Red: Write failing test first
        String aidHex = "A0 00 00 00 03 10 10";
        
        // Green: Create AID from hex string
        AID aid = new AID(aidHex);
        
        // Verify
        assertThat(aid).isNotNull();
        assertThat(aid.getAIDBytes()).isEqualTo(Util.fromHexString(aidHex));
    }
    
    @Test
    public void shouldCreateAIDFromByteArray() {
        byte[] aidBytes = {(byte) 0xA0, 0x00, 0x00, 0x00, 0x03, 0x10, 0x10};
        
        AID aid = new AID(aidBytes);
        
        assertThat(aid.getAIDBytes()).isEqualTo(aidBytes);
    }
    
    @Test
    public void shouldValidateAIDLength() {
        // AID must be 5-16 bytes according to ISO7816-4
        
        // Test minimum length (5 bytes)
        byte[] minAID = {(byte) 0xA0, 0x00, 0x00, 0x00, 0x03};
        assertThatNoException().isThrownBy(() -> new AID(minAID));
        
        // Test maximum length (16 bytes)
        byte[] maxAID = new byte[16];
        maxAID[0] = (byte) 0xA0;
        assertThatNoException().isThrownBy(() -> new AID(maxAID));
        
        // Test invalid length (too short)
        byte[] tooShort = {(byte) 0xA0, 0x00, 0x00, 0x00};
        assertThatThrownBy(() -> new AID(tooShort))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid AID length");
        
        // Test invalid length (too long)
        byte[] tooLong = new byte[17];
        assertThatThrownBy(() -> new AID(tooLong))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid AID length");
    }
    
    @Test
    public void shouldExtractRIDFromAID() {
        // RID (Registered Application Provider Identifier) is first 5 bytes
        byte[] expectedRID = {(byte) 0xA0, 0x00, 0x00, 0x00, 0x03};
        
        RID rid = visaAID.getRID();
        
        assertThat(rid.getRIDBytes()).isEqualTo(expectedRID);
    }
    
    @Test
    public void shouldDetectPartialMatch() {
        AID fullAID = new AID("A0 00 00 00 03 10 10 05 28");
        AID partialAID = new AID("A0 00 00 00 03 10 10");
        
        boolean matches = fullAID.partialMatch(partialAID);
        
        assertThat(matches).isTrue();
    }
    
    @Test
    public void shouldNotMatchDifferentAIDs() {
        boolean matches = visaAID.partialMatch(mastercardAID);
        
        assertThat(matches).isFalse();
    }
    
    @Test
    public void shouldImplementEqualsAndHashCode() {
        AID aid1 = new AID("A0 00 00 00 03 10 10");
        AID aid2 = new AID("A0 00 00 00 03 10 10");
        AID aid3 = new AID("A0 00 00 00 04 10 10");
        
        // Test equals
        assertThat(aid1).isEqualTo(aid2);
        assertThat(aid1).isNotEqualTo(aid3);
        assertThat(aid1).isNotEqualTo(null);
        assertThat(aid1).isNotEqualTo("not an AID");
        
        // Test hashCode
        assertThat(aid1.hashCode()).isEqualTo(aid2.hashCode());
    }
    
    @Test
    public void shouldProvideStringRepresentation() {
        String aidString = visaAID.toString();
        
        // The actual AID toString() uses lowercase hex and different format
        assertThat(aidString).contains("a0 00 00 00 03 10 10");
    }
    
    @Test
    public void shouldHandleNullInput() {
        assertThatThrownBy(() -> new AID((byte[]) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("AID cannot be null");
            
        assertThatThrownBy(() -> new AID((String) null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("AID cannot be null");
    }
    
    @Test
    public void shouldHandleEmptyInput() {
        assertThatThrownBy(() -> new AID(new byte[0]))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid AID length");
            
        assertThatThrownBy(() -> new AID(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid AID length");
    }
} 