package com.example.demo.service;

import com.example.demo.model.Device;
import com.example.demo.model.Room;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TelegramBotService extends TelegramLongPollingBot {
    private final RoomService roomService;
    private final DeviceService deviceService;
    private final DeviceControlService deviceControlService;
    
    @Value("${telegram.bot.token}")
    private String botTokenValue;
    
    @Value("${telegram.bot.username}")
    private String botUsernameValue;
    
    public TelegramBotService(
            @Value("${telegram.bot.token}") String botToken,
            DeviceService deviceService, 
            DeviceControlService deviceControlService, RoomService roomService) {
        super(botToken);
        this.roomService = roomService;
        this.deviceService = deviceService;
        this.deviceControlService = deviceControlService;
        this.botTokenValue = botToken;
    }
    
    @PostConstruct
    public void init() {
        log.info("Telegram Bot Initialize. Name: {}, Token: {}", 
                botUsernameValue, 
                botTokenValue != null && !botTokenValue.isEmpty());
    }
    
    @Override
    public String getBotUsername() {
        return botUsernameValue; 
    }
    
    @Override
    public String getBotToken() {
        return botTokenValue;
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            
            log.info("Message from {}: {}", update.getMessage().getFrom().getUserName(), messageText);
            
            if (messageText.equals("/start")) {
                sendWelcomeWithButton(chatId);
            } else if (messageText.equals("Статус дома")) { 
                sendHouseStatus(chatId);
            } else {
                sendSimpleMessage(chatId, "Нажмите /start чтобы начать");
            }
        }
    }
    
    private void sendWelcomeWithButton(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Нажмите кнопку ниже, чтобы узнать статус дома:");
        message.enableMarkdown(true);
        
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);
        
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton("Статус дома");
        row.add(button);
        keyboard.add(row);
        
        keyboardMarkup.setKeyboard(keyboard);
        message.setReplyMarkup(keyboardMarkup);
        
        sendMessage(message);
    }
    
    private void sendHouseStatus(long chatId) {
        try {
            List<Device> allDevices = deviceService.getAllDevices();
            long activeDevices = allDevices.stream().filter(Device::isActive).count();
            double totalPower = deviceControlService.getTotalPowerConsumption();
            List<Room> rooms = roomService.getAllRooms();
            StringBuilder rom = new StringBuilder();
            for (Room room : rooms) {
                rom.append(room.getLocation()).append(", ");
            }
            String status = String.format(
                "*Статус Умного Дома:*\n" +
                " Устройств всего: %d\n" +
                " Сейчас работает: %d\n" +
                " Потребление: %.1f Вт\n" +
                " Комнаты: %s\n",
                allDevices.size(), 
                activeDevices, 
                totalPower,
                rom
            );
            
            sendSimpleMessage(chatId, status);
            
        } catch (Exception e) {
            log.error("Error: {}", e.getMessage());
            sendSimpleMessage(chatId, "Не могу получить данные");
        }
    }
    
    private void sendSimpleMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        message.enableMarkdown(true);
        sendMessage(message);
    }
    
    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Send error: {}", e.getMessage());
        }
    }
}