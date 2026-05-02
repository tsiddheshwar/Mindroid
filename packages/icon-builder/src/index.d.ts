export interface IconBuilderOptions {
    inputPngPath: string;
    outputResDir: string;
    iconName?: string;
    backgroundColor?: string;
    generateVector?: boolean;
}
export declare function buildAndroidIcons(options: IconBuilderOptions): Promise<void>;
