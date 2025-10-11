package www.ppkok.com.jyfac;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;

/**
 * Jyfac插件服务类
 * 负责获取和管理Jyfac插件的配置数据
 * 
 * @author Jyf
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class JyfacService {
    
    private final ReactiveSettingFetcher settingFetcher;
    
    /**
     * 获取完整的Jyfac插件配置
     * 
     * @return 完整配置的Mono对象
     */
    public Mono<JyfacSetting> getCompleteSetting() {
        return Mono.zip(
                settingFetcher.fetch("basic", JyfacSetting.class).onErrorReturn(JyfacSetting.defaultSetting()),
                settingFetcher.fetch("content", JyfacSetting.class).onErrorReturn(JyfacSetting.defaultSetting()),
                settingFetcher.fetch("content-style", JyfacSetting.class).onErrorReturn(JyfacSetting.defaultSetting()),
                settingFetcher.fetch("icon-style", JyfacSetting.class).onErrorReturn(JyfacSetting.defaultSetting())
            )
            .map(tuple -> {
                var basic = tuple.getT1();
                var content = tuple.getT2();
                var contentStyle = tuple.getT3();
                var iconStyle = tuple.getT4();
                
                // 合并所有配置组的设置
                return new JyfacSetting(
                    basic.enabled(),
                    content.text(),
                    basic.position(),
                    basic.showIcon(),
                    iconStyle.iconType(),
                    iconStyle.customSvg(),
                    iconStyle.imageUrl(),
                    basic.allowClose(),
                    contentStyle.autoHideDelay(),
                    basic.displayMode(),
                    content.multiLineTexts(),
                    basic.autoScroll(),
                    contentStyle.scrollInterval(),
                    contentStyle.backgroundColor(),
                    contentStyle.textColor(),
                    contentStyle.borderRadius(),
                    contentStyle.zIndex()
                );
            })
            .onErrorReturn(JyfacSetting.defaultSetting());
    }
    
    /**
     * 检查Jyfac插件是否启用
     * 
     * @return 是否启用的Mono对象
     */
    public Mono<Boolean> isEnabled() {
        return getCompleteSetting()
            .map(JyfacSetting::enabled)
            .onErrorReturn(false);
    }
}