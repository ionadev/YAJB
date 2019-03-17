import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Random;

class BotListener extends ListenerAdapter
{

    public static void main(String[] args)
    {
        String token = System.getenv("TOKEN");
        try {
            JDA jda = new JDABuilder(token)
                    .addEventListeners(new BotListener())
                    .build();
            jda.awaitReady();
            System.out.println("[ JDA ] BUILD - Finished");
        } catch (LoginException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        JDA jda = event.getJDA();
        long responseNumber = event.getResponseNumber();

        User author = event.getAuthor();
        Message message = event.getMessage();
        MessageChannel channel = event.getChannel();

        String msg = message.getContentDisplay();

        boolean bot = author.isBot();

        if(event.isFromType(ChannelType.TEXT))
        {
            Guild guild = event.getGuild();
            TextChannel textChannel = event.getTextChannel();
            Member member = event.getMember();

            String name;
            if(message.isWebhookMessage())
            {
                name = author.getName();
            }
            else
                {
                    name = member.getEffectiveName();
                }

        }

        if(msg.equals("ping"))
        {
            channel.sendMessage("Yo").queue();
        }
        else if(msg.equals("!roll"))
        {
            Random rand = new Random();
            int roll = rand.nextInt(6) + 1;
            channel.sendMessage("Your roll: "+roll).queue(sentMessage ->
            {
                if(roll < 3)
                {
                    channel.sendMessage("The Rolled Number For MessageID "+sentMessage.getId() + " want very good..... Its Bad Luck!");
                }
            });
        }
        else if(msg.startsWith("!!kick"))
        {
            if(message.isFromType(ChannelType.TEXT))
            {
                if(message.getMentionedUsers().isEmpty())
                {
                    channel.sendMessage("You Must Mention One Or More Users To Kick!").queue();
                }
                else
                {
                    Guild guild = event.getGuild();
                    Member selfMember = guild.getSelfMember();

                    if(!selfMember.hasPermission(Permission.KICK_MEMBERS))
                    {
                        channel.sendMessage("I Dont Have permissions to Kick Members");
                    }

                    List<User> mentionedUsers = message.getMentionedUsers();

                    for(User user: mentionedUsers)
                    {
                        Member member = guild.getMember(user);
                        if(!selfMember.canInteract(member))
                        {
                            channel.sendMessage("Cannot kick member: ")
                                    .append(member.getEffectiveName())
                                    .append(", they are higher in the hierarchy than I am!")
                                    .queue();
                            continue;
                        }

                        guild.getController().kick(member).queue(
                                success -> channel.sendMessage("Kicked ")
                                .append(member.getEffectiveName())
                                .append("!").queue(),
                                error ->
                                {
                                    if(error instanceof PermissionException)
                                    {
                                        PermissionException pe = (PermissionException) error;
                                        Permission missingPermission = pe.getPermission();

                                        channel.sendMessage("PermissionError Kicking [")
                                                .append(member.getEffectiveName())
                                                .append("]: ")
                                                .append(error.getMessage())
                                                .queue();
                                    }
                                    else
                                        {
                                            channel.sendMessage("Unknown Error While Kicking [")
                                                    .append(member.getEffectiveName())
                                                    .append("]: <")
                                                    .append(error.getClass().getSimpleName())
                                                    .append(">: ")
                                                    .append(error.getMessage())
                                                    .queue();
                                        }
                                }
                        );
                    }
                }
            }
            else
            {
                channel.sendMessage("This Is a Guild Only Command!").queue();
            }
        }
    }
}
