package org.ezhik.authtgem;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
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
                    String message = "<b>Добро пожаловать!</b>\n";
                    String chatId = update.getMessage().getChatId().toString();

                    if (isUserRegistered(chatId)){
                        String playername = getPlayerNameByChatId(chatId);
                        Player player = getPlayerByName(playername);

                        // Если игрок онлайн
                        if (player != null && player.isOnline()) {
                            String status = "<b>Онлайн</b>";
                            sendMessage(update.getMessage().getChatId(), message + "Ваш никнейм: <b>" + playername + "</b>\n" + "Статус: " + status);
                        } else {
                            // Если игрок не онлайн, показываем время последней активности
                            User user = User.getUser(playername);
                            String lastActivity = user != null ? user.last_activity : null;
                            String status = "<b>был в сети: " + TimeUtils.getTimeAgo(lastActivity) + " назад</b>";
                            sendMessage(update.getMessage().getChatId(), message + "Ваш никнейм: <b>" + playername + "</b>\n" + "Статус: " + status);
                        }


                    }else{
                        sendMessage(update.getMessage().getChatId(), message+"<b>Напишите /reg чтобы зарегистрировать аккаунт.</b>");
                    }
                }
                if (update.getMessage().getText().toString().equals("/reg")) {
                    nextStep.put(update.getMessage().getChatId().toString(), "askplayername");
                    sendMessage(update.getMessage().getChatId(), "Зайдите на сервер, после чего напишите свой никнейм из <b>Minecraft</b>");
                }
                if (update.getMessage().getText().toString().equals("/help")) {
                    sendMessage(update.getMessage().getChatId(), "Если у вас возникли какие-либо проблемы, вы можете обратиться в поддержку написав в комментарии канала - <b>@StillWaterCraft</b>.");
                }
                if (update.getMessage().getText().toString().equals("/login")){
                    String chatId = update.getMessage().getChatId().toString();
                    if (isUserRegistered(chatId)){
                        String playername = getPlayerNameByChatId(chatId);
                        Player onlinePlayer = Bukkit.getPlayer(playername);
                        if (onlinePlayer != null && onlinePlayer.isOnline()){
                            sendMessage(update.getMessage().getChatId(), "<b>Вы уже на сервере!</b>");
                            return;
                        }
                        OfflinePlayer player = Bukkit.getOfflinePlayer(playername);
                        if (player.isOp()){
                            sendMessage(update.getMessage().getChatId(), "<b>Эта функция отключена для операторов в качестве безопасности.</b>");
                            return;
                        }
                        System.out.println("4");

                        System.out.println("5");
                        LoginManager.addToLogin(playername);
                        sendMessage(update.getMessage().getChatId(), "<b>Готово! В течении 5 минут вы можете зайти в игру без подтверждения.</b>");

                    } else{
                        sendMessage(update.getMessage().getChatId(), "<b>Сначала зарегистрируйтесь через /reg</b>");
                        return;
                    }

                }

            }
            if (nextStep.containsKey(update.getMessage().getChatId().toString())) {
                if (nextStep.get(update.getMessage().getChatId().toString()).equals("askplayername")) {
                    if (User.isNickname(update.getMessage().getText().toString())) {
                        Player player = Bukkit.getPlayer(update.getMessage().getText().toString());
                        if (player == null) {
                            sendMessage(update.getMessage().getChatId(), "<b>Игрок с таким именем не найден на сервере.</b> Убедитесь, что имя введено правильно.");
                            return;
                        }
                        UUID uuid = player.getUniqueId();
                        User user = User.getUser(uuid);
                        sendMessage(update.getMessage().getChatId(), "<b>Ожидайте... Выполняется проверка...</b>");
                        if (User.getChatID(update.getMessage().getChatId().toString())) {
                            sendMessage(update.getMessage().getChatId(), "<b>Вы уже привязали аккаунт к Telegram</b>");
                        }else{
                            if (user != null) {
                                if (user.chatid.equals(update.getMessage().getChatId())) {
                                    this.sendMessage(update.getMessage().getChatId(), "<b>Вы не можете привязать больше одного аккаунта.</b>");
                                } else {
                                    this.sendMessage(update.getMessage().getChatId(), "<b>Аккаунт Minecraft уже привязан к другому Telegram</b>");
                                }
                            } else {
                                User.register(update.getMessage(), uuid);
                                sendMessage(update.getMessage().getChatId(), "<b>Аккаунт успешно привязан!</b>");
                            }
                        }
                    }
                }
            }
        }
        if (update.hasCallbackQuery()) {
            if (update.getCallbackQuery().getData().toString().startsWith("ACCEPT_TRUE_REAL")) {
                String playername = update.getCallbackQuery().getData().toString().replace("ACCEPT_TRUE_REAL", "");
                FreezerEvent.unfreezeplayer(playername);
                MuterEvent.unmute(playername);
                System.out.println(playername);
                Player player = Bukkit.getPlayer(playername);
                player.resetTitle();
                Long ChatId1 = update.getCallbackQuery().getMessage().getChatId();
                this.deleteMessage(update.getCallbackQuery().getMessage());

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&f&l[&b&lAuthTG&f&l] &a&lУспешный вход в аккаунт"));
                sendMessage(ChatId1, "<b>Успешный вход в аккаунт</b>");

            }
            if (update.getCallbackQuery().getData().toString().startsWith("ACCEPT_FALSE_REAL")) {
                String playername = update.getCallbackQuery().getData().toString().replace("ACCEPT_FALSE_REAL", "");
                Handler.kick(playername, ChatColor.translateAlternateColorCodes('&', "Вы отклонили запрос на вход."));
                Long ChatId1 = update.getCallbackQuery().getMessage().getChatId();
                this.deleteMessage(update.getCallbackQuery().getMessage());
                sendMessage(ChatId1, "<b>Вход отклонен</b>");
            }
        }
    }
    public void sendMessage(Long Chatid, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(Chatid);
        sendMessage.setText(message);
        sendMessage.setParseMode("HTML");
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

    // Метод для проверки, зарегистрирован ли пользователь
    private static boolean isUserRegistered(String chatId) {
        return User.getChatID(chatId);
    }

    // Метод для получения никнейма игрока по chatId
    private static String getPlayerNameByChatId(String chatId) {
        List<User> users = User.getUserList();
        for (User user : users) {
            if (user.chatid.toString().equals(chatId)) {
                return user.playername;
            }
        }
        return null;
    }

    private Player getPlayerByName(String playerName) {
        return Bukkit.getPlayer(playerName);
    }

}