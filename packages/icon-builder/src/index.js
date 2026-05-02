import { mkdir, writeFile } from "node:fs/promises";
import { dirname, join } from "node:path";
import sharp from "sharp";
const DENSITIES = [
    { suffix: "mdpi", size: 48 },
    { suffix: "hdpi", size: 72 },
    { suffix: "xhdpi", size: 96 },
    { suffix: "xxhdpi", size: 144 },
    { suffix: "xxxhdpi", size: 192 }
];
async function writeText(path, content) {
    await mkdir(dirname(path), { recursive: true });
    await writeFile(path, content, "utf8");
}
export async function buildAndroidIcons(options) {
    const iconName = options.iconName ?? "ic_launcher";
    const backgroundColor = options.backgroundColor ?? "#FFFFFF";
    for (const density of DENSITIES) {
        const mipmapDir = join(options.outputResDir, `mipmap-${density.suffix}`);
        await mkdir(mipmapDir, { recursive: true });
        await sharp(options.inputPngPath)
            .resize(density.size, density.size, { fit: "cover" })
            .png()
            .toFile(join(mipmapDir, `${iconName}.png`));
        await sharp(options.inputPngPath)
            .resize(Math.floor(density.size * 0.78), Math.floor(density.size * 0.78), { fit: "cover" })
            .png()
            .toFile(join(mipmapDir, `${iconName}_foreground.png`));
    }
    const anydpiDir = join(options.outputResDir, "mipmap-anydpi-v26");
    await mkdir(anydpiDir, { recursive: true });
    await writeText(join(anydpiDir, `${iconName}.xml`), `<?xml version="1.0" encoding="utf-8"?>\n<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n    <background android:drawable="@color/${iconName}_background" />\n    <foreground android:drawable="@mipmap/${iconName}_foreground" />\n</adaptive-icon>\n`);
    await writeText(join(options.outputResDir, "values", `${iconName}_colors.xml`), `<?xml version="1.0" encoding="utf-8"?>\n<resources>\n    <color name="${iconName}_background">${backgroundColor}</color>\n</resources>\n`);
    if (options.generateVector) {
        await writeText(join(options.outputResDir, "drawable", `${iconName}_vector.xml`), `<?xml version="1.0" encoding="utf-8"?>\n<vector xmlns:android="http://schemas.android.com/apk/res/android"\n    android:width="108dp"\n    android:height="108dp"\n    android:viewportWidth="108"\n    android:viewportHeight="108">\n    <path\n        android:fillColor="${backgroundColor}"\n        android:pathData="M12,12h84v84h-84z" />\n</vector>\n`);
        await writeText(join(options.outputResDir, "drawable", `${iconName}_bitmap.xml`), `<?xml version="1.0" encoding="utf-8"?>\n<bitmap xmlns:android="http://schemas.android.com/apk/res/android"\n    android:src="@mipmap/${iconName}"\n    android:gravity="center" />\n`);
    }
}
//# sourceMappingURL=index.js.map