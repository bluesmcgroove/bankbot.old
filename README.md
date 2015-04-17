##  Summary

**BankBot is in the works of being stripped down to only necessary functions and is not intended to be a moderation bot**
BankBot is a modification of CoeBot, a fork of Geobot. 
GeoBot is a Java-based IRC bot based on the
[PIRC](http://www.jibble.org/pircbot.php) framework designed for [Twitch](http://twitch.tv).

##  Features

  * Twitch API integration
  * Custom Triggers and Auto replies
  * Open Source!

##  Issues/Feature Requests

Open a [new issue](https://github.com/bluesmcgroove/bankbot) on GitHub.

##  Commands

###  Note about prefixes

This documentation uses the default command prefix of `!`. This can be changed on the channel level using: `!set prefix`.

### Syntax
  * `<>` - Denotes required arguments
  * `[]` - Denotes optional arguments
  * `option1|option2`- Denotes basic toggles.

###  General Channel

  * `!join` - Request bot to join your channel 
  * `!rejoin` - Force bot to attempt to rejoin your channel 
  * `!leave` - Owner - Removes the bot from channel 
  * `!viewers` - Displays the number of Twitch viewers. 
  * `!chatters` - Displays the number of Twitch chatters.
  * `!uptime` - Displays stream starting time and length of time streamed. 
  * `!bothelp` - Displays link to bot help documents 
  * `!status [new status]` - Displays the current Twitch status. Optional - specify a new status to set (must be channel editor).
  * `!followme` - Request the bot follow your Twitch account. Can only be done in your own channel.

  
### Currency

  * `!balance` or `bal` - Returns the sending user's balance.
  * `!currency set <name> <number>` - Sets a specified user's currency balance
  * `!currency clear <name>` - Sets a user's balance to the default
  * `!currency get <name>` - Returns a user's balance in chat. `!curr get bluesmcgroove` will return `The balance of bluesmcgroove is 1234 Points`
  * `!currency add <name> <number>` -  Adds `<number>` to the specified user's current balance
  * `!currency remove <name> <number>` -  Subtracts `<number>` to the specified user's current balance


###  Custom, Custom Repeat, Custom Scheduled, Auto-Replies

#### Triggers
Custom commands aka triggers provide frequently requested information in your channel.  

  * `!command add <name> <text>` - Creates an info command (!name) 
  * `!command delete <name>` - Removes info command (!name)
  * `!command restrict <name> <everyone|regulars|mods|owner>` - Sets access level for triggers.

#### Repeat
The repeat command will repeat a custom trigger every X amount of seconds passed. Message difference allows you to prevent spamming an inactive channel. It requires Y amount of messages have passed in the channel since the last iteration of the message. The default is 1 so at least one message will need to have been sent in the channel in order for the repeat to trigger.  

  * `!repeat add <name> <delay in seconds> [message difference]` - Sets a command to repeat.
  * `!repeat delete <name>` - Stops repetition of a command and discards info.
  * `!repeat on|off <name>` - Enables/disables repetition of a command but keeps info.
  * `!repeat list` - Lists commands that will be repeated.

#### Schedule 
Schedule is similar to repeat but is designed to repeat at specific times such as 5pm, hourly (on the hour), semihourly (on 0:30), etc. `pattern` accepts: hourly, semihourly, and [crontab syntax**](http://i.imgur.com/j4t8CcM.png). **Replace spaces in crontab syntax with _ (underscore)**


  * `!schedule add <name> <pattern> [message difference]` - Schedules a command.
  * `!schedule delete <name>` - Removes scheduled command and discards info.
  * `!schedule on/off <name>` - Enables/disables scheduled command but keeps info.
  * `!schedule list` - Lists schedules commands.

Examples:

 * `!schedule add youtube hourly 0` - This will repeat the `!youtube` command every hour on the hour.
 * `!schedule add ip *_*_*_*_* 0` - This will repeat the `!ip` command every minute.
 * `!schedule add texture *_5_*_*_* 0` - This will repeat the `!texture` at 5am every day.

#### Auto-Replies
Autoreplies are like custom triggers but do not require a command to be typed. The bot will check all messages for the specified pattern and reply with the response if found. **Responses have a 30 second cooldown**

 * `!autoreply list` - Lists current autoreplies
 * `!autoreply add [patten] [response]` - Adds an autoreply triggered by \*pattern\* with the desired response. Use * to denote wildcards and _ to denote spaces in the pattern.
 * `!autoreply remove [number]` - Removes the autoreply with that index number. Do !autoreply list for those values.

Example:

`!autoreply add *what*texture* The broadcaster is using Sphax.` will respond with: `The broadcaster is using Sphax.` if a message similar to: `What texture pack is this?` is typed.

### Settings
  * `!set <option> [parameters]`
  * **Options**: 
    * `mode <(0/owner)|(1/mod)|(2,everyone)|(-1, "Admin mode")>` - Sets the minimum access to use any bot commands. 
    * `commerciallength <30|60|90|120|150|180>` - Length of commercials to run with.
    * `tweet <message>` - Format for Click to tweet message.
    * `prefix <character>` - Sets the command prefix. Default is "!"
    * `emoteset <set id>` - Sets the emote_set for of the subscription product for this channel. (Used to determine subscriber status for regulars)
    * `subscriberregulars on|off` - Treats subscribers a regulars. `emoteset` must be defined first.
    * `subscriberalerts on|off` - Toggle chat alert when a new user subs.
    * `subscriberalerts message <message>` - Message to be displayed when new user subs. Use `(_1_)` to insert the new subscriber's name.
    * `currency <message>` - Set the string you wish your currency to be named. `!set currency BluePoints` would 

### User Levels

Consists of Owners, Mods, and Regulars. Owner have permission to you all channel bot commands. Mods have permission to use moderation related commands. Regulars are immune to the link filter. **Mods are optional if you only wish to use Twitch mod status**

  * `!owner|mod|regular list` - Lists users in that group.
  * `!owner|mod|regular add|remove <name>` - Adds/removes a user from the group.


### String Replacement

Adding dynamic data to bot message is also supported via string substitutions. Almost any response from the bot will accept a replacement. The following substitutions are available:

  * `(_GAME_)` : Twitch game.
  * `(_STATUS_)` or `(_JTV_STATUS_)`: Channel status.
  * `(_VIEWERS_)` or `(_JTV_VIEWERS_)` : Viewer count.
  * `(_BOT_HELP_)` : Bot's help message. See bothelpMessage in global.properties..
  * `(_TWEET_URL_)` : Click to tweet URL See !set tweet.
  * `(_USER_)` : Nick of the user requesting a trigger or triggering an autoreply.

  Example:
  `!command add info I am (_STEAM_PROFILE_) and I'm playing (_STEAM_GAME_) on (_STEAM_SERVER_) listening to (_SONG_)`

  Output:
  `I am http://bit.ly/yoursteamprofile and I'm playing ARMA III on 127.0.0.1:2602 listening to Wings of Destiny by David Saulesco`

###  Admin

Admin nicks are defined in global.properties. Twitch Admins and Staff also have access.

  * `!admin join [#channelname]` - Joins channelname. (Note: Forces mode level -1)
  * `!admin part [#channelname]` - Leaves channelname
  * `!admin color [color]` - Changes the bot name color to a specified color