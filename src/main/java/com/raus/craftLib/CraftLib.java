package com.raus.craftLib;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

public class CraftLib extends JavaPlugin
{
	// Useful shortcuts
	public final Material[] banners = {
			Material.WHITE_BANNER, Material.ORANGE_BANNER, Material.MAGENTA_BANNER, Material.LIGHT_BLUE_BANNER,
			Material.YELLOW_BANNER, Material.LIME_BANNER, Material.PINK_BANNER, Material.GRAY_BANNER,
			Material.LIGHT_GRAY_BANNER, Material.CYAN_BANNER, Material.PURPLE_BANNER, Material.BLUE_BANNER,
			Material.BROWN_BANNER, Material.GREEN_BANNER, Material.RED_BANNER, Material.BLACK_BANNER
	};

	private List<ExactRecipe> recipes = new ArrayList<>();
	private Set<NamespacedKey> keys = new HashSet<>();

	public static class Pair
	{
		public final Material material;
		public final NamespacedKey key;

		public Pair(Material material, NamespacedKey key)
		{
			this.material = material;
			this.key = key;
		}
	}

	@Override
	public void onEnable()
	{
		// Listeners
		getServer().getPluginManager().registerEvents(new CraftingListener(), this);


		NamespacedKey demeraldKey = new NamespacedKey(this, "demerald");
		NamespacedKey demeraldSwordKey = new NamespacedKey(this, "demerald_sword");
		NamespacedKey enchantedBookKey = new NamespacedKey(this, "enchanted_book");
		NamespacedKey mergeEnchantKey = new NamespacedKey(this, "merge_enchant");

		// Demerald item
		ItemStack item1 = new ItemStack(Material.EMERALD);
		ItemMeta meta1 = item1.getItemMeta();
		meta1.setDisplayName("Demerald");
		meta1.getPersistentDataContainer().set(demeraldKey, PersistentDataType.BYTE, (byte) 0);
		item1.setItemMeta(meta1);

		// Demerald recipe
		ShapelessRecipe recipe1 = new ShapelessRecipe(demeraldKey, item1);
		recipe1.addIngredient(Material.DIAMOND);
		recipe1.addIngredient(Material.EMERALD);
		Bukkit.addRecipe(recipe1);

		addKey(demeraldKey);


		// Demerald sword
		ItemStack item2 = new ItemStack(Material.DIAMOND_SWORD);
		ItemMeta meta2 = item2.getItemMeta();
		meta2.setDisplayName("Demerald Sword");
		meta2.getPersistentDataContainer().set(demeraldSwordKey, PersistentDataType.BYTE, (byte) 0);
		item2.setItemMeta(meta2);

		// Demerald sword recipe
		ExactShapedRecipe recipe2 = new ExactShapedRecipe(demeraldSwordKey, item2);
		recipe2.setIngredient('e', Material.EMERALD, demeraldKey);
		recipe2.setIngredient('s', Material.STICK);
		recipe2.setShape("e", "e", "s");
		addRecipe(recipe2);


		// Enchanted book recipe
		ExactShapelessRecipe recipe3 = new ExactShapelessRecipe(enchantedBookKey, new ItemStack(Material.ENCHANTED_BOOK));
		recipe3.addIngredient(Material.EXPERIENCE_BOTTLE);
		recipe3.addIngredient(Material.BOOK);
		recipe3.setCallback(event ->
		{
			ItemStack result = event.getInventory().getResult();

			EnchantmentStorageMeta meta = (EnchantmentStorageMeta) result.getItemMeta();
			if (Math.random() > 0.5)
			{
				meta.addStoredEnchant(Enchantment.DURABILITY, 1, true);
			}
			else
			{
				meta.addStoredEnchant(Enchantment.DAMAGE_ALL, 1, true);
			}
			result.setItemMeta(meta);

			event.getInventory().setResult(result);
		});
		addRecipe(recipe3);

		// Merge enchantments
		ExactShapelessRecipe recipe4 = new ExactShapelessRecipe(mergeEnchantKey, item2);
		recipe4.addIngredient(Material.DIAMOND_SWORD, demeraldSwordKey);
		recipe4.addIngredient(Material.ENCHANTED_BOOK);
		recipe4.setCallback(event ->
		{
			ItemStack result = event.getInventory().getResult();
			ItemMeta meta = result.getItemMeta();

			// Iterate through crafting slots
			for (ItemStack slot : event.getInventory().getMatrix())
			{
				// Find enchantment book
				if (slot != null && slot.getType() == Material.ENCHANTED_BOOK)
				{
					// Transfer enchantments
					EnchantmentStorageMeta enchMeta = (EnchantmentStorageMeta) slot.getItemMeta();
					for (Map.Entry<Enchantment, Integer> ench : enchMeta.getStoredEnchants().entrySet())
					{
						meta.addEnchant(ench.getKey(), ench.getValue(), false);
					}
				}
			}
			result.setItemMeta(meta);

			event.getInventory().setResult(result);
		});
		addRecipe(recipe4);
	}

	@Override
	public void onDisable()
	{

	}

	/*
	 * Adds custom recipes to CraftLib.
	 *
	 * @param recipe the custom recipe to be added
	 */
	public void addRecipe(ExactShapedRecipe recipe)
	{
		recipes.add(recipe);
		addKey(recipe.getKey());

		// We'll let Bukkit tell us if the recipe has been formed, then we decide which one it is
		ShapedRecipe bukkitRecipe = new ShapedRecipe(recipe.getKey(), recipe.getResult());
		bukkitRecipe.shape(recipe.getShape());
		for (Map.Entry<Character, CraftLib.Pair> entry : recipe.getIngredientMap().entrySet())
		{
			bukkitRecipe.setIngredient(entry.getKey(), entry.getValue().material);

			// Remember custom keys
			addKey(entry.getValue().key);
		}
		Bukkit.addRecipe(bukkitRecipe);
	}

	/*
	 * Adds custom recipes to CraftLib.
	 *
	 * @param recipe the custom recipe to be added
	 */
	public void addRecipe(ExactShapelessRecipe recipe)
	{
		recipes.add(recipe);
		addKey(recipe.getKey());

		// We'll let Bukkit tell us if the recipe has been formed, then we decide which one it is
		ShapelessRecipe bukkitRecipe = new ShapelessRecipe(recipe.getKey(), recipe.getResult());
		for (Pair pair : recipe.getIngredientList())
		{
			bukkitRecipe.addIngredient(pair.material);

			// Remember custom keys
			addKey(pair.key);
		}
		Bukkit.addRecipe(bukkitRecipe);
	}

	/*
	 * If you want custom items to be treated separately from
	 * regular items in crafting recipes, you can add its
	 * NamespacedKey to CraftLib.
	 *
	 * Custom items found in recipes added to CraftLib will be
	 * automatically handled.
	 *
	 * @param key the custom item's namespaced key
	 */
	public void addKey(NamespacedKey key)
	{
		if (key != null) { keys.add(key); }
	}

	public List<ExactRecipe> getRecipes()
	{
		return ImmutableList.copyOf(recipes);
	}

	public Set<NamespacedKey> getKeys()
	{
		return ImmutableSet.copyOf(keys);
	}
}