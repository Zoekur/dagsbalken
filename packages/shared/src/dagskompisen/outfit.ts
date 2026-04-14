import type { WeatherContext } from "../data/models.js";

export interface OutfitDescriptor {
  baseName: string;
  hairName?: string;
  topName: string;
  bottomName: string;
  shoesName: string;
  hatName?: string;
}

const COLD_THRESHOLD = 5;
const HOT_THRESHOLD = 25;
const PRECIPITATION_THRESHOLD = 30;

export function resolveOutfit(ctx: WeatherContext, precipitationChance = 0): OutfitDescriptor {
  const temp = ctx.temperatureC;
  const isSnow = ctx.condition === "SNOW";
  const isRain = ctx.condition === "RAIN" || ctx.condition === "STORM";
  const isWinter = temp !== undefined && temp <= COLD_THRESHOLD;
  const isHot = temp !== undefined && temp >= HOT_THRESHOLD;
  const isWet = precipitationChance >= PRECIPITATION_THRESHOLD || isRain;

  if (isSnow || (isWinter && isWet)) {
    return {
      baseName: "base_winter",
      topName: "coat_winter",
      bottomName: "jeans_g",
      shoesName: "boots_winter",
      hatName: "hat_winter",
    };
  }

  if (isWinter) {
    return {
      baseName: "base_winter",
      topName: "coat_winter",
      bottomName: "jeans_g",
      shoesName: "boots_winter",
      hatName: "hat_winter",
    };
  }

  if (isHot && isWet) {
    return {
      baseName: "base_light",
      hairName: "hair_default",
      topName: "raincoat",
      bottomName: "jorts",
      shoesName: "sneakers",
    };
  }

  if (isHot) {
    return {
      baseName: "base_light",
      hairName: "hair_default",
      topName: "shirt_light",
      bottomName: "jorts",
      shoesName: "sneakers",
    };
  }

  if (isWet) {
    return {
      baseName: "base_default",
      topName: "raincoat",
      bottomName: "jeans",
      shoesName: "boots_grey",
    };
  }

  // Standard
  return {
    baseName: "base_default",
    hairName: "hair_default",
    topName: "shirt_light",
    bottomName: "jeans",
    shoesName: "sneakers",
  };
}

export function assistantMessage(ctx: WeatherContext): string {
  const temp = ctx.temperatureC;

  if (temp !== undefined) {
    if (temp <= 0) return "Det är minusgrader idag – ta på dig ordentligt!";
    if (temp <= COLD_THRESHOLD) return "Kallt ute, klä dig varmt.";
    if (temp >= HOT_THRESHOLD) return "Riktigt varmt idag – lättare kläder!";
  }

  switch (ctx.condition) {
    case "RAIN":
    case "STORM":
      return "Ta med regnjacka idag.";
    case "SNOW":
      return "Det snöar – vinterkläder är ett måste!";
    case "SUN":
      return "Soligt väder, ha en trevlig dag!";
    case "WINDY":
      return "Blåsigt idag, ta en vindtät jacka.";
    default:
      return "Ha en bra dag!";
  }
}
