package org.ezhik.authtgem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.ezhik.authtgem.events.FreezerEvent;
import org.ezhik.authtgem.events.MuterEvent;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.*;

public class BotTelegram extends TelegramLongPollingBot {
    private String username = "changeme";
    private String token = "changeme";
    private static Map<String, String> nextStep = new HashMap<>();
    private static Map<String, UUID> playerUUID = new HashMap<>();
    private Map<String, String> sendMessageData = new HashMap<>();
    public static Map<String, String> curentplayer = new HashMap<>();

    public BotTelegram() {
        YamlConfiguration config = new YamlConfiguration();
        File file = new File("plugins/Minetelegram/config.yml");
        if (!file.exists()) {
            config.set("username", username);
            config.set("token", token);
            try {
                config.save(file);
            } catch (Exception e) {
                System.out.println("Error creating config file: " + e);
            }
        } else {
            try {
                config.load(file);
            } catch (IOException e) {
                System.out.println("Error loading config file: " + e);
            } catch (InvalidConfigurationException e) {
                System.out.println("Error loading config file: " + e);
            }
            username = config.getString("username");
            token = config.getString("token");
        }


    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().getText().toString().startsWith("/")) {
                if (update.getMessage().getText().toString().equals("/start")) {
                    sendMessage(update.getMessage().getChatId(), "Добро пожаловать! Напишите /reg чтобы зарегистрировать аккаунт.");
                }
                if (update.getMessage().getText().toString().equals("/reg")) {
                    nextStep.put(update.getMessage().getChatId().toString(), "askplayername");
                    sendMessage(update.getMessage().getChatId(), "Напишите свой никнейм из Minecraft");
                }

            }
            if (nextStep.containsKey(update.getMessage().getChatId().toString())) {
                if (nextStep.get(update.getMessage().getChatId().toString()).equals("askplayername")) {
                    if (User.isNickname(update.getMessage().getText().toString())) {
                        Player player = Bukkit.getPlayer(update.getMessage().getText().toString());
                        UUID uuid = player.getUniqueId();
                        User user = User.getUser(uuid);
                        sendMessage(update.getMessage().getChatId(), "Ожидайте... Выполняется проверка...");
                        if (User.getChatID(update.getMessage().getChatId().toString())) {
                            sendMessage(update.getMessage().getChatId(), "Вы уже привязали аккаунт к Telegram");
                        }else{
                            if (user != null) {
                                if (user.chatid.equals(update.getMessage().getChatId())) {
                                    this.sendMessage(update.getMessage().getChatId(), "Аккаунт уже привязан к вам");
                                } else {
                                    this.sendMessage(update.getMessage().getChatId(), "Аккаунт уже привязан к другому аккаунту Minecraft");
                                }
                            } else {
                                User.register(update.getMessage(), uuid);
                            }
                        }
                    }
                }
            }
        }
        if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().toString().startsWith("ys")) {
                String playername = update.getCallbackQuery().getData().toString().replace("ys", "");
                FreezerEvent.unfreezeplayer(playername);
                MuterEvent.unmute(playername);
                Player player = Bukkit.getPlayer(playername);
                player.resetTitle();
                Long ChatId1 = update.getCallbackQuery().getMessage().getChatId();
                this.deleteMessage(update.getCallbackQuery().getMessage());

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&b&lAuthTG&f&l] &a&lУспешный вход в аккаунт"));
                sendMessage(ChatId1, "Успешный вход в аккаунт");

            }
            if (update.getCallbackQuery().getData().toString().startsWith("no")) {
                String playername = update.getCallbackQuery().getData().toString().replace("no", "");
                Handler.kick(playername, ChatColor.translateAlternateColorCodes('&', "Вы были кикнуты через Telegram"));
                Long ChatId1 = update.getCallbackQuery().getMessage().getChatId();
                this.deleteMessage(update.getCallbackQuery().getMessage());
                sendMessage(ChatId1, "Вход отклонен");
            }
        }
    }
    public void sendMessage(Long Chatid, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(Chatid);
        sendMessage.setText(message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("Error sending message: " + e);
        }
    }
    public void deleteMessage(Message message) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(message.getChatId());
        deleteMessage.setMessageId(message.getMessageId());
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            System.out.println("Error deleting message: " + e);
        }
    }

}