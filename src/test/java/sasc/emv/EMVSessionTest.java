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
package sasc.emv;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import sasc.smartcard.common.SmartCard;
import sasc.terminal.CardConnection;
import sasc.terminal.CardResponse;
import sasc.terminal.TerminalException;
import sasc.iso7816.ATR;
import sasc.iso7816.AID;
import sasc.iso7816.SmartCardException;
import sasc.util.Util;

/**
 * Test Driven Development tests for EMV Session Management
 * Following Red-Green-Refactor cycle
 * 
 * @author sasc
 */
public class EMVSessionTest {
    
    @Mock
    private SmartCard mockCard;
    
    @Mock
    private CardConnection mockTerminal;
    
    @Mock
    private CardResponse mockResponse;
    
    @Mock
    private EMVApplication mockApplication;
    
    private EMVSession session;
    private ATR testATR;
    
    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        
        // Create test ATR
        byte[] atrBytes = Util.fromHexString("3B 68 00 00 00 73 C8 40 12 00 90 00");
        testATR = new ATR(atrBytes);
        
        // Setup mock card
        when(mockCard.getATR()).thenReturn(testATR);
        when(mockCard.getEmvApplications()).thenReturn(java.util.Collections.emptyList());
        
        // Setup mock terminal responses
        when(mockResponse.getSW1()).thenReturn((byte) 0x90);
        when(mockResponse.getSW2()).thenReturn((byte) 0x00);
        when(mockResponse.getData()).thenReturn(new byte[0]);
        
        session = EMVSession.startSession(mockCard, mockTerminal);
    }
    
    @After
    public void tearDown() {
        session = null;
    }
    
    @Test
    public void shouldCreateSessionWithValidParameters() {
        // Red: Test session creation
        assertThat(session).isNotNull();
        assertThat(session.getCard()).isEqualTo(mockCard);
    }
    
    @Test
    public void shouldThrowExceptionForNullCard() {
        // Red: Test null validation
        assertThatThrownBy(() -> EMVSession.startSession(null, mockTerminal))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Arguments cannot be null");
    }
    
    @Test
    public void shouldThrowExceptionForNullTerminal() {
        // Red: Test null validation
        assertThatThrownBy(() -> EMVSession.startSession(mockCard, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Arguments cannot be null");
    }
    
    @Test
    public void shouldInitializeContextSuccessfully() throws TerminalException {
        // Setup PSE response
        byte[] pseResponse = Util.fromHexString("6F 20 84 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31 A5 0E 88 01 02 5F 2D 04 6E 6F 65 6E 9F 11 01 01");
        when(mockTerminal.transmit(any(byte[].class))).thenReturn(mockResponse);
        when(mockResponse.getData()).thenReturn(pseResponse);
        when(mockResponse.getSW1()).thenReturn((byte) 0x90);
        when(mockResponse.getSW2()).thenReturn((byte) 0x00);
        
        // Green: Initialize context
        session.initContext();
        
        // Verify SELECT PSE command was sent
        verify(mockTerminal, atLeastOnce()).transmit(any(byte[].class));
    }
    
    @Test
    public void shouldHandlePSENotAvailable() throws TerminalException {
        // Setup failure response for PSE
        when(mockTerminal.transmit(any(byte[].class))).thenReturn(mockResponse);
        when(mockResponse.getSW1()).thenReturn((byte) 0x6A);
        when(mockResponse.getSW2()).thenReturn((byte) 0x82); // File not found
        when(mockResponse.getData()).thenReturn(new byte[0]);
        
        // Should not throw exception
        assertThatNoException().isThrownBy(() -> session.initContext());
        
        // Should try PPSE as fallback
        verify(mockTerminal, atLeast(2)).transmit(any(byte[].class));
    }
    
    @Test
    public void shouldSelectApplicationSuccessfully() throws TerminalException {
        // First initialize context
        when(mockTerminal.transmit(any(byte[].class))).thenReturn(mockResponse);
        when(mockResponse.getData()).thenReturn(new byte[10]);
        when(mockResponse.getSW1()).thenReturn((byte) 0x90);
        when(mockResponse.getSW2()).thenReturn((byte) 0x00);
        session.initContext();
        
        // Setup application
        AID testAID = new AID("A0 00 00 00 03 10 10");
        when(mockApplication.getAID()).thenReturn(testAID);
        when(mockApplication.getCard()).thenReturn(mockCard);
        
        // Setup SELECT AID response
        byte[] selectResponse = Util.fromHexString("6F 1E 84 07 A0 00 00 00 03 10 10 A5 13 50 07 56 49 53 41 43 52 45 87 01 01");
        when(mockResponse.getData()).thenReturn(selectResponse);
        
        session.selectApplication(mockApplication);
        
        verify(mockCard).setSelectedApplication(mockApplication);
    }
    
    @Test
    public void shouldThrowExceptionForApplicationSelectionFailure() throws TerminalException {
        // First initialize context
        when(mockTerminal.transmit(any(byte[].class))).thenReturn(mockResponse);
        when(mockResponse.getData()).thenReturn(new byte[10]);
        when(mockResponse.getSW1()).thenReturn((byte) 0x90);
        when(mockResponse.getSW2()).thenReturn((byte) 0x00);
        session.initContext();
        
        AID testAID = new AID("A0 00 00 00 03 10 10");
        when(mockApplication.getAID()).thenReturn(testAID);
        
        // Setup failure response for application selection
        when(mockResponse.getSW1()).thenReturn((byte) 0x6A);
        when(mockResponse.getSW2()).thenReturn((byte) 0x82);
        
        assertThatThrownBy(() -> session.selectApplication(mockApplication))
            .isInstanceOf(SmartCardException.class)
            .hasMessageContaining("Failed to select application");
    }
    
    @Test
    public void shouldInitiateApplicationProcessing() throws TerminalException {
        // Setup selected application with proper state
        when(mockCard.getSelectedApplication()).thenReturn(mockApplication);
        when(mockApplication.isInitializedOnICC()).thenReturn(false); // Not yet initialized
        when(mockApplication.getCard()).thenReturn(mockCard);
        
        // Setup GET PROCESSING OPTIONS response
        byte[] gpoResponse = Util.fromHexString("77 0E 82 02 19 78 94 08 08 01 01 00 10 01 01 00");
        when(mockTerminal.transmit(any(byte[].class))).thenReturn(mockResponse);
        when(mockResponse.getData()).thenReturn(gpoResponse);
        when(mockResponse.getSW1()).thenReturn((byte) 0x90);
        when(mockResponse.getSW2()).thenReturn((byte) 0x00);
        
        // Mock the card to not throw exception
        doNothing().when(mockCard).setSelectedApplication(any());
        
        session.initiateApplicationProcessing();
        
        verify(mockTerminal).transmit(any(byte[].class));
    }
    
    @Test
    public void shouldThrowExceptionWhenNoApplicationSelected() {
        when(mockCard.getSelectedApplication()).thenReturn(null);
        
        assertThatThrownBy(() -> session.initiateApplicationProcessing())
            .isInstanceOf(SmartCardException.class)
            .hasMessageContaining("No application selected");
    }
    
    @Test
    public void shouldPrepareTransactionProcessing() throws TerminalException {
        // Setup application in processing state
        when(mockCard.getSelectedApplication()).thenReturn(mockApplication);
        when(mockApplication.isInitializedOnICC()).thenReturn(true);
        when(mockApplication.isAllAppRecordsInAFLRead()).thenReturn(true);
        
        // Mock ApplicationInterchangeProfile to avoid null pointer
        ApplicationInterchangeProfile aip = mock(ApplicationInterchangeProfile.class);
        when(mockApplication.getApplicationInterchangeProfile()).thenReturn(aip);
        when(aip.isCDASupported()).thenReturn(false);
        
        session.prepareTransactionProcessing();
        
        // Should complete without exception
    }
    
    @Test
    public void shouldPerformTransaction() throws TerminalException {
        // Setup application in transaction ready state
        when(mockCard.getSelectedApplication()).thenReturn(mockApplication);
        when(mockApplication.isInitializedOnICC()).thenReturn(true);
        when(mockApplication.isAllAppRecordsInAFLRead()).thenReturn(true);
        
        // Mock ApplicationInterchangeProfile to avoid null pointer
        ApplicationInterchangeProfile aip = mock(ApplicationInterchangeProfile.class);
        when(mockApplication.getApplicationInterchangeProfile()).thenReturn(aip);
        when(aip.isCDASupported()).thenReturn(false);
        
        session.performTransaction();
        
        // Should complete transaction processing without exception
    }
    
    @Test
    public void shouldHandleContextInitializationOnlyOnce() throws TerminalException {
        // Setup first initialization
        when(mockTerminal.transmit(any(byte[].class))).thenReturn(mockResponse);
        when(mockResponse.getData()).thenReturn(new byte[10]);
        when(mockResponse.getSW1()).thenReturn((byte) 0x90);
        when(mockResponse.getSW2()).thenReturn((byte) 0x00);
        
        // Initialize context first time
        session.initContext();
        
        // Try to initialize again - should throw exception
        assertThatThrownBy(() -> session.initContext())
            .isInstanceOf(SmartCardException.class)
            .hasMessageContaining("EMV context already initalized");
    }
    
    @Test
    public void shouldProvideAccessToCard() {
        SmartCard card = session.getCard();
        
        assertThat(card).isEqualTo(mockCard);
    }
    
    // Test helper methods removed - inline mocking used instead
    
    @Test
    public void shouldHandleMultipleApplications() throws TerminalException {
        // Initialize context first
        when(mockTerminal.transmit(any(byte[].class))).thenReturn(mockResponse);
        when(mockResponse.getData()).thenReturn(new byte[10]);
        when(mockResponse.getSW1()).thenReturn((byte) 0x90);
        when(mockResponse.getSW2()).thenReturn((byte) 0x00);
        session.initContext();
        
        // Setup multiple applications
        EMVApplication app1 = mock(EMVApplication.class);
        EMVApplication app2 = mock(EMVApplication.class);
        
        AID aid1 = new AID("A0 00 00 00 03 10 10");
        AID aid2 = new AID("A0 00 00 00 04 10 10");
        
        when(app1.getAID()).thenReturn(aid1);
        when(app2.getAID()).thenReturn(aid2);
        when(app1.getCard()).thenReturn(mockCard);
        when(app2.getCard()).thenReturn(mockCard);
        
        // Process both applications
        session.selectApplication(app1);
        session.selectApplication(app2);
        
        verify(mockCard, times(2)).setSelectedApplication(any(EMVApplication.class));
    }
    
    @Test
    public void shouldValidateApplicationStateTransitions() throws TerminalException {
        // Initialize context first
        when(mockTerminal.transmit(any(byte[].class))).thenReturn(mockResponse);
        when(mockResponse.getData()).thenReturn(new byte[10]);
        when(mockResponse.getSW1()).thenReturn((byte) 0x90);
        when(mockResponse.getSW2()).thenReturn((byte) 0x00);
        session.initContext();
        
        // 1. Select application
        when(mockApplication.getAID()).thenReturn(new AID("A0 00 00 00 03 10 10"));
        when(mockApplication.getCard()).thenReturn(mockCard);
        
        session.selectApplication(mockApplication);
        
        // 2. Initiate processing
        when(mockCard.getSelectedApplication()).thenReturn(mockApplication);
        when(mockApplication.isInitializedOnICC()).thenReturn(false);
        
        session.initiateApplicationProcessing();
        
        // Verify state transitions occurred
        verify(mockCard, atLeastOnce()).setSelectedApplication(mockApplication);
    }
} 