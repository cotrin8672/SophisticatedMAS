package io.github.cotrin8672.sophisticatedmas.registrate

import com.tterrag.registrate.AbstractRegistrate
import net.minecraftforge.eventbus.api.IEventBus
import thedarkcolour.kotlinforforge.forge.MOD_BUS

class KotlinRegistrate
private constructor(modId: String) : AbstractRegistrate<KotlinRegistrate>(modId) {
    companion object {
        fun create(modId: String): KotlinRegistrate = KotlinRegistrate(modId)
    }

    override fun getModEventBus(): IEventBus {
        return MOD_BUS
    }

    public override fun registerEventListeners(bus: IEventBus): KotlinRegistrate {
        return super.registerEventListeners(bus) as KotlinRegistrate
    }
}