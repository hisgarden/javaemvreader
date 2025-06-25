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
import java.io.ByteArrayInputStream;
import java.util.List;

/**
 * Test Driven Development tests for BER-TLV (Basic Encoding Rules Tag-Length-Value)
 * Following Red-Green-Refactor cycle
 * 
 * @author sasc
 */
public class BERTLVTest {
    
    private byte[] simpleTagData;
    private byte[] complexTagData;
    private byte[] nestedTagData;
    
    @Before
    public void setUp() {
        // 50 03 50 49 4E - Tag 50 (Application Label), Length 3, Value "PIN"
        simpleTagData = Util.fromHexString("50 03 50 49 4E");
        
        // 6F 1E 84 07 A0 00 00 00 04 30 60 A5 13 50 07 4D 41 45 53 54 52 4F 87 01 02 9F 38 09 9F 66 04 9F 02 06 9F 37 04
        // FCI Template with nested tags
        complexTagData = Util.fromHexString("6F 1E 84 07 A0 00 00 00 04 30 60 A5 13 50 07 4D 41 45 53 54 52 4F 87 01 02 9F 38 09 9F 66 04 9F 02 06 9F 37 04");
        
        // Simplified nested TLV structure - 70 17 (tag 70, length 23) with child 50 15 (tag 50, length 21)
        nestedTagData = Util.fromHexString("70 17 50 15 56 49 53 41 20 44 65 62 69 74 2F 43 72 65 64 69 74 20 28 43 6C 61 73 73 69 63 29");
    }
    
    @Test
    public void shouldParseSimpleTLV() {
        ByteArrayInputStream stream = new ByteArrayInputStream(simpleTagData);
        
        BERTLV tlv = TLVUtil.getNextTLV(stream);
        
        assertThat(tlv).isNotNull();
        assertThat(tlv.getTag().getTagBytes()).isEqualTo(new byte[]{0x50});
        assertThat(tlv.getLength()).isEqualTo(3);
        assertThat(tlv.getValueBytes()).isEqualTo(new byte[]{0x50, 0x49, 0x4E});
    }
    
    @Test
    public void shouldParseComplexTLV() {
        ByteArrayInputStream stream = new ByteArrayInputStream(complexTagData);
        
        BERTLV tlv = TLVUtil.getNextTLV(stream);
        
        assertThat(tlv).isNotNull();
        assertThat(tlv.getTag().getTagBytes()).isEqualTo(new byte[]{0x6F});
        assertThat(tlv.getLength()).isEqualTo(0x1E);
        assertThat(tlv.getTag().isConstructed()).isTrue();
    }
    
    @Test
    public void shouldParseNestedTLVStructure() {
        ByteArrayInputStream stream = new ByteArrayInputStream(nestedTagData);
        BERTLV rootTlv = TLVUtil.getNextTLV(stream);
        
        assertThat(rootTlv).isNotNull();
        assertThat(rootTlv.getTag().getTagBytes()).isEqualTo(new byte[]{0x70});
        assertThat(rootTlv.getTag().isConstructed()).isTrue();
        
        // Parse children from value bytes
        ByteArrayInputStream childStream = new ByteArrayInputStream(rootTlv.getValueBytes());
        BERTLV child = TLVUtil.getNextTLV(childStream);
        assertThat(child).isNotNull();
    }
    
    @Test
    public void shouldHandleShortFormLength() {
        // Length in short form (0-127)
        byte[] shortFormData = Util.fromHexString("50 05 48 65 6C 6C 6F"); // "Hello"
        ByteArrayInputStream stream = new ByteArrayInputStream(shortFormData);
        
        BERTLV tlv = TLVUtil.getNextTLV(stream);
        
        assertThat(tlv.getLength()).isEqualTo(5);
        assertThat(new String(tlv.getValueBytes())).isEqualTo("Hello");
    }
    
    @Test
    public void shouldHandleLongFormLength() {
        // Length in long form (> 127)
        // 81 82 means length is encoded in next 2 bytes (0x0082 = 130)
        byte[] longFormHeader = Util.fromHexString("50 81 82");
        byte[] value = new byte[130]; // 130 bytes of data
        byte[] longFormData = new byte[longFormHeader.length + value.length];
        System.arraycopy(longFormHeader, 0, longFormData, 0, longFormHeader.length);
        System.arraycopy(value, 0, longFormData, longFormHeader.length, value.length);
        
        ByteArrayInputStream stream = new ByteArrayInputStream(longFormData);
        
        BERTLV tlv = TLVUtil.getNextTLV(stream);
        
        assertThat(tlv.getLength()).isEqualTo(130);
        assertThat(tlv.getValueBytes()).hasSize(130);
    }
    
    @Test
    public void shouldDetectConstructedTags() {
        // Tag 0x6F is constructed (bit 6 = 1)
        byte[] constructedData = Util.fromHexString("6F 05 50 03 50 49 4E");
        ByteArrayInputStream stream = new ByteArrayInputStream(constructedData);
        
        BERTLV tlv = TLVUtil.getNextTLV(stream);
        
        assertThat(tlv.getTag().isConstructed()).isTrue();
    }
    
    @Test
    public void shouldDetectPrimitiveTags() {
        // Tag 0x50 is primitive (bit 6 = 0)
        ByteArrayInputStream stream = new ByteArrayInputStream(simpleTagData);
        
        BERTLV tlv = TLVUtil.getNextTLV(stream);
        
        assertThat(tlv.getTag().isConstructed()).isFalse();
    }
    
    @Test
    public void shouldHandleMultibyteTag() {
        // Tag 9F38 (multi-byte tag)
        byte[] multibyteTagData = Util.fromHexString("9F 38 05 48 65 6C 6C 6F");
        ByteArrayInputStream stream = new ByteArrayInputStream(multibyteTagData);
        
        BERTLV tlv = TLVUtil.getNextTLV(stream);
        
        assertThat(tlv.getTag().getTagBytes()).isEqualTo(new byte[]{(byte) 0x9F, 0x38});
        assertThat(tlv.getLength()).isEqualTo(5);
    }
    
    @Test
    public void shouldFindChildByTag() {
        ByteArrayInputStream stream = new ByteArrayInputStream(complexTagData);
        BERTLV fciTemplate = TLVUtil.getNextTLV(stream);
        
        // Parse children to find Application Label (tag 50)
        ByteArrayInputStream childStream = new ByteArrayInputStream(fciTemplate.getValueBytes());
        while (childStream.available() > 0) {
            BERTLV child = TLVUtil.getNextTLV(childStream);
            if (child.getTag().getTagBytes()[0] == 0x50) {
                assertThat(new String(child.getValueBytes())).isEqualTo("MAESTRO");
                return;
            }
        }
        org.junit.Assert.fail("Application Label tag not found");
    }
    
    @Test
    public void shouldHandleEmptyValue() {
        byte[] emptyValueData = Util.fromHexString("50 00");
        ByteArrayInputStream stream = new ByteArrayInputStream(emptyValueData);
        
        BERTLV tlv = TLVUtil.getNextTLV(stream);
        
        assertThat(tlv.getLength()).isEqualTo(0);
        assertThat(tlv.getValueBytes()).isEmpty();
    }
    
    @Test
    public void shouldThrowExceptionOnInvalidData() {
        // Truncated data - tag present but no length/value
        byte[] invalidData = new byte[]{0x50};
        ByteArrayInputStream stream = new ByteArrayInputStream(invalidData);
        
        assertThatThrownBy(() -> TLVUtil.getNextTLV(stream))
            .isInstanceOf(TLVException.class);
    }
    
    @Test
    public void shouldHandleZeroLengthStream() {
        ByteArrayInputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        
        assertThatThrownBy(() -> TLVUtil.getNextTLV(emptyStream))
            .isInstanceOf(TLVException.class);
    }
    
    @Test
    public void shouldProvideHumanReadableString() {
        ByteArrayInputStream stream = new ByteArrayInputStream(simpleTagData);
        BERTLV tlv = TLVUtil.getNextTLV(stream);
        
        String tlvString = tlv.toString();
        
        assertThat(tlvString).containsIgnoringCase("50");
    }
    
    @Test
    public void shouldCalculateCorrectBytesLength() {
        ByteArrayInputStream stream = new ByteArrayInputStream(complexTagData);
        BERTLV fciTemplate = TLVUtil.getNextTLV(stream);
        
        int expectedLength = fciTemplate.getLength() + fciTemplate.getTag().getTagBytes().length + fciTemplate.getRawEncodedLengthBytes().length;
        
        assertThat(fciTemplate.toBERTLVByteArray().length).isEqualTo(expectedLength);
    }
    
    @Test
    public void shouldSerializeBackToBytes() {
        ByteArrayInputStream stream = new ByteArrayInputStream(simpleTagData);
        BERTLV tlv = TLVUtil.getNextTLV(stream);
        
        byte[] serialized = tlv.toBERTLVByteArray();
        
        assertThat(serialized).isEqualTo(simpleTagData);
    }
} 