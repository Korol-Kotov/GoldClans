package me.korolkotov.goldclans.util

import com.destroystokyo.paper.profile.ProfileProperty
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import java.util.UUID
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.text.split

class ItemBuilder(
    private val item: ItemStack
) {
    private val meta: ItemMeta? = if (item.hasItemMeta()) item.itemMeta else Bukkit.getItemFactory().getItemMeta(item.type)

    fun amount(value: Int) = apply {
        item.amount = value.coerceIn(1, item.maxStackSize)
    }

    fun name(value: String?) = apply {
        if (value != null)
            meta?.displayName(MessageService.format(value).asComponent())
    }

    fun lore(value: List<String>?) = apply {
        if (!value.isNullOrEmpty())
            meta?.lore(value.map { MessageService.format(it).asComponent() })
    }

    fun customModelData(value: Int?) = apply {
        if (value != null && value >= 0)
            meta?.setCustomModelData(value)
    }

    fun unbreakable(value: Boolean) = apply {
        meta?.isUnbreakable = value
    }

    fun enchants(map: Map<String, Int>?) = apply {
        map?.forEach { (key, level) ->
            val enchant = Enchantment.getByKey(NamespacedKey.minecraft(key.lowercase())) ?: return@forEach
            meta?.addEnchant(enchant, level.coerceAtLeast(1), true)
        }
    }

    fun itemFlags(flags: List<String>?) = apply {
        flags?.forEach {
            runCatching { ItemFlag.valueOf(it.uppercase()) }
                .onSuccess { flag -> meta?.addItemFlags(flag) }
        }
    }

    fun skull(owner: String?, texture: String?) = apply {
        if (meta !is SkullMeta) return@apply

        when {
            owner != null -> {
                val profile = Bukkit.createProfile(Bukkit.getOfflinePlayer(owner).uniqueId, owner)
                meta.playerProfile = profile
            }

            texture != null -> {
                val profile = Bukkit.createProfile(UUID.randomUUID())
                profile.setProperty(ProfileProperty("textures", texture))
                meta.playerProfile = profile
            }
        }
    }

    fun leatherColor(hex: String?) = apply {
        if (meta !is LeatherArmorMeta || hex == null) return@apply

        val color = Color.fromRGB(Integer.parseInt(hex.removePrefix("#"), 16))
        meta.setColor(color)
    }

    fun potion(section: ConfigurationSection?) = apply {
        if (meta !is PotionMeta || section == null) return@apply

        val type = section.getString("type")?.let { PotionType.valueOf(it.uppercase()) }
        if (type != null) meta.basePotionType = type

        section.getStringList("effects").forEach {
            val (name, duration, amplifier) = it.split(":")
            val effect = PotionEffectType.getByName(name.uppercase()) ?: return@forEach
            meta.addCustomEffect(
                PotionEffect(effect, duration.toInt(), amplifier.toInt()),
                true
            )
        }
    }

    fun data(section: ConfigurationSection?) = apply {
        section?.getKeys(false)?.forEach { key ->
            val namespacedKey = NamespacedKey.minecraft(key)
            when (val value = section.get(key)) {
                is String -> meta?.persistentDataContainer?.set(
                    namespacedKey,
                    PersistentDataType.STRING,
                    value
                )

                is Int -> meta?.persistentDataContainer?.set(
                    namespacedKey,
                    PersistentDataType.INTEGER,
                    value
                )
            }
        }
    }

    fun build(): ItemStack {
        if (meta != null) item.itemMeta = meta
        return item
    }

    companion object {
        fun fromConfig(section: ConfigurationSection): ItemStack {
            val material = Material.matchMaterial(section.getString("material")!!)
                ?: throw RuntimeException("Not found material for ${section.getString("material")}")

            return ItemBuilder(ItemStack(material))
                .amount(section.getInt("amount", 1))
                .name(section.getString("display-name"))
                .lore(section.getStringList("lore"))
                .customModelData(section.getInt("custom-model-data").takeIf { it >= 0 })
                .unbreakable(section.getBoolean("unbreakable"))
                .itemFlags(section.getStringList("item-flags"))
                .enchants(section.getConfigurationSection("enchants")?.getValues(false)?.mapValues {
                    (it.value as Number).toInt()
                })
                .skull(
                    section.getString("skull.owner"),
                    section.getString("skull.texture")
                )
                .leatherColor(section.getString("leather-color"))
                .potion(section.getConfigurationSection("potion"))
                .data(section.getConfigurationSection("data"))
                .build()
        }
    }
}