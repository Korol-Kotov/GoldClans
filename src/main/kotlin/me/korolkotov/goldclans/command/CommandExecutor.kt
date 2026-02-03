package me.korolkotov.goldclans.command

import me.korolkotov.goldclans.annotations.SubCommand
import me.korolkotov.goldclans.config.ConfigManager
import me.korolkotov.goldclans.logger.Logger
import me.korolkotov.goldclans.util.MessageService
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.hasAnnotation

abstract class CommandExecutor : TabExecutor {
    private val commandMethods = mutableMapOf<String, MutableSet<CommandWrapper>>()

    init {
        registerCommands()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val subCommand = args.getOrNull(0)
        val args1 = args.getOrNull(1)

        val wrappers = getCommandWrappers(subCommand, args1)
        for (wrapper in wrappers) {
            var index = if (subCommand != null) 1 else 0

            if (wrapper.getCommand().subCommands.isNotEmpty()) index++

            val result = runCommand(sender, wrapper, args.copyOfRange(index, args.size))
            Logger.instance.debug("${sender.name} ran command ${wrapper.getCommand().commands.firstOrNull() ?: "use (blank)"} with result ${result.name}.")

            when (result) {
                CommandResult.NO_PERMISSIONS -> {
                    MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.errorsConfig.notEnoughPerms)
                    return true
                }
                CommandResult.ONLY_PLAYER -> {
                    MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.errorsConfig.onlyForPlayer)
                    return true
                }
                CommandResult.METHOD_ERROR -> {
                    val command = if (wrapper.getCommand().subCommands.isEmpty()) wrapper.getCommand().commands.firstOrNull() ?: "use"
                    else wrapper.getCommand().commands.first() + "." + wrapper.getCommand().subCommands.first()
                    MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.errorsConfig.notEnoughArgs,
                        mapOf("%usage%" to MessageService.format(ConfigManager.instance.messageConfig.helpConfig.getMessage(command))))
                    return true
                }
                CommandResult.COMMAND_ERROR -> {
                    MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.errorsConfig.somethingWentWrong)
                    return true
                }
                CommandResult.SUCCESS -> return true
            }
        }

        return true
    }


    private fun runCommand(sender: CommandSender, wrapper: CommandWrapper, args: Array<out String>): CommandResult {
        val command = wrapper.getCommand()

        runCatching {
            if (command.permissionNode.isNotEmpty() &&
                !sender.hasPermission("blackauction.${command.permissionNode}")
            ) return CommandResult.NO_PERMISSIONS

            val function = wrapper.function
            val funcParams = function.parameters.toMutableList()
            funcParams.removeFirstOrNull()
            val requestedParams = funcParams.toList()
            val params = Array<Any?>(requestedParams.size) { null }
            var argCount = args.size

            if (sender !is Player && requestedParams[0].type.classifier == Player::class)
                return CommandResult.ONLY_PLAYER

            params[0] = sender as? Player ?: sender

            val lastParam = requestedParams.lastOrNull()
            if (lastParam != null && lastParam.type.classifier == Array<String>::class) {
                argCount = requestedParams.size - 2
                val varParamCount = args.size - argCount

                val varParams = if (varParamCount == 0) emptyArray<String>() else args.copyOfRange(argCount, args.size)
                params[params.size - 1] = varParams
            }

            val requiredArgsCount = requestedParams
                .drop(1)
                .count { !it.isOptional && !it.type.isMarkedNullable }

            if (args.size < requiredArgsCount) {
                return CommandResult.METHOD_ERROR
            }

            var commandFound = true
            for (i in 0..<argCount) {
                if (requestedParams.size - 1 <= i) break

                val param = requestedParams[i + 1]
                val obj = verifyArgument(args[i], param.type.classifier as KClass<*>)

                if (obj == null && (!param.isOptional && !param.type.isMarkedNullable)) {
                    commandFound = false
                    break
                } else {
                    params[i + 1] = obj
                }
            }

            if (commandFound) function.call(this, *params)

            return CommandResult.SUCCESS
        }.onFailure { e ->
            Logger.instance.error(
                "Произошла ошибка при попытке вызвать команду." +
                        "\nОтправитель: ${sender.name}; Команда: ${wrapper.getCommand().commands.firstOrNull() ?: "use (blank)"}; Аргументы: ${args.joinToString(", ")}",
                e
            )
        }

        return CommandResult.COMMAND_ERROR
    }

    fun sendHelpMessage(sender: CommandSender) {
        val availableCommands = mutableListOf<CommandWrapper>()
        commandMethods.values.forEach { wrappers ->
            wrappers.filter { sender.hasPermission("blackauction.${it.getCommand().permissionNode}") }
                .forEach(availableCommands::add)
        }

        if (availableCommands.isEmpty()) {
            MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.errorsConfig.notEnoughPerms)
            return
        }

        MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.helpConfig.header)
        for (wrapper in availableCommands) {
            val command = wrapper.getCommand()
            val name = if (command.subCommands.isEmpty()) command.commands.firstOrNull() ?: "use" else command.commands.first() + "." + command.subCommands.first()
            MessageService.sendMessage(sender, ConfigManager.instance.messageConfig.helpConfig.getMessage(name))
        }
    }

    fun getCommandWrappers(command: String?, subCommand: String?): List<CommandWrapper> {
        val wrappers = mutableListOf<CommandWrapper>()

        for (wraps in commandMethods.values) {
            for (wrapper in wraps) {
                val annotation = wrapper.getCommand()

                if (command != null && !annotation.commands.contains(command)
                    || command == null && annotation.commands.isNotEmpty()) continue

                if (annotation.subCommands.isEmpty() || annotation.subCommands.contains(subCommand)) {
                    wrappers.add(wrapper)
                }
            }
        }

        return wrappers
    }

    private fun registerCommands() {
        for (function in this::class.functions) {
            if (function.hasAnnotation<SubCommand>() && function.returnType.classifier == Unit::class) {
                val command = function.findAnnotation<SubCommand>()!!

                val wrapper = CommandWrapper(function)
                if (command.commands.isEmpty()) {
                    val wrappers = commandMethods.getOrDefault("use", mutableSetOf())
                    wrappers.add(wrapper)

                    commandMethods["use"] = wrappers
                }

                for (cmd in command.commands) {
                    val wrappers = commandMethods.getOrDefault(cmd, mutableSetOf())
                    wrappers.add(wrapper)

                    commandMethods[cmd] = wrappers
                }
            }
        }
    }

    private fun verifyArgument(arg: String, parameter: KClass<*>): Any? {
        return when (parameter.simpleName?.lowercase()) {
            "string" -> arg
            "int" -> arg.toIntOrNull()
            "long" -> arg.toLongOrNull()
            "double" -> arg.toDoubleOrNull()
            "float" -> arg.toFloatOrNull()
            "boolean" -> {
                when (arg) {
                    "true", "yes", "on" -> true
                    "false", "no", "off" -> false
                    else -> null
                }
            }
            "material" -> Material.entries.firstOrNull { it.name.equals(arg, true) }
            "player" -> Bukkit.getPlayerExact(arg)
            "offlineplayer" -> Bukkit.getOfflinePlayerIfCached(arg)
            "world" -> Bukkit.getWorld(arg)
            else -> null
        }
    }

    private fun verifyTabComplete(arg: String, parameter: KClass<*>): List<String> {
        var completions = mutableListOf<String>()
        when (parameter.simpleName?.lowercase()) {
            "material" -> {
                completions = Material.entries.map { it.name }.toMutableList()
            }
            "player" -> {
                completions = Bukkit.getOnlinePlayers().map { it.name }.toMutableList()
            }
            "offlineplayer" -> {
                completions = Bukkit.getOfflinePlayers().mapNotNull { it.name }.toMutableList()
            }
            "world" -> {
                completions = Bukkit.getWorlds().map { it.name }.toMutableList()
            }
        }
        completions.removeIfNotStartsWith(arg)

        return completions
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        val completions = mutableListOf<String>()

        runCatching {
            if (args.size == 1) {
                for (cmd in commandMethods.keys) {
                    if (cmd.equals("use", true)) continue
                    val wrapper = commandMethods[cmd].orEmpty().firstOrNull() ?: continue
                    val command = wrapper.getCommand()

                    if (command.permissionNode.isNotEmpty() && !sender.hasPermission("blackauction.${command.permissionNode}"))
                        continue

                    completions.add(cmd)
                }
                completions.removeIfNotStartsWith(args[0])
            }

            if (args.size == 2) {
                if (!commandMethods.containsKey(args[0]))
                    return mutableListOf()
            }

            if (args.size > 1) {
                val wrappers = commandMethods[args[0]]
                for (wrapper in wrappers.orEmpty()) {
                    val command = wrapper.getCommand()
                    if (command.permissionNode.isNotEmpty() && !sender.hasPermission("blackauction.${command.permissionNode}"))
                        continue

                    if (args.size == 2 && command.subCommands.isNotEmpty())
                        completions.addAll(command.subCommands)
                    else {
                        val funcParams = wrapper.function.parameters.toMutableList()
                        funcParams.removeFirstOrNull()
                        val requestedParams = funcParams.toList()
                        if (requestedParams.size < args.size)
                            continue

                        var requestedParam = requestedParams[args.size - 1]
                        if (command.subCommands.isNotEmpty() && command.subCommands.contains(args[1]))
                            requestedParam = requestedParams[args.size - 2]

                        completions.addAll(
                            verifyTabComplete(
                                args[args.size - 1],
                                requestedParam.type.classifier as KClass<*>
                            )
                        )
                    }
                }
                completions.removeIfNotStartsWith(args[args.size - 1])
            }
        }.onFailure {
            TODO("logging")
        }

        return completions
    }

    private fun MutableList<String>.removeIfNotStartsWith(value: String) {
        val lower = value.lowercase()
        for (completion in this.toList()) {
            if (!completion.lowercase().startsWith(lower))
                this.remove(completion)
        }
    }

    class CommandWrapper(
        val function: KFunction<*>
    ) {
        fun getCommand(): SubCommand {
            return function.findAnnotation<SubCommand>()!!
        }
    }

    enum class CommandResult {
        NO_PERMISSIONS,
        COMMAND_ERROR,
        ONLY_PLAYER,
        METHOD_ERROR,
        SUCCESS;
    }
}