package www.ppkok.com.jyfac;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.model.IModel;
import org.thymeleaf.model.IModelFactory;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.IElementModelStructureHandler;
import reactor.core.publisher.Mono;
import run.halo.app.theme.dialect.TemplateHeadProcessor;

/**
 * Jyfac插件头部处理器
 * 在HTML头部注入Jyfac插件相关的CSS、JS和HTML资源
 * 
 * @author Jyf
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class JyfacHeadProcessor implements TemplateHeadProcessor {
    
    private final JyfacService jyfacService;
    
    @Override
    public Mono<Void> process(ITemplateContext context, IModel model,
                             IElementModelStructureHandler structureHandler) {
        
        return jyfacService.isEnabled()
            .filter(enabled -> enabled)
            .flatMap(enabled -> jyfacService.getCompleteSetting())
            .doOnNext(setting -> {
                IModelFactory modelFactory = context.getModelFactory();
                
                // 注入CSS样式
                String cssContent = generateCssContent(setting);
                IProcessableElementTag styleOpenTag = modelFactory.createOpenElementTag("style");
                model.add(styleOpenTag);
                model.add(modelFactory.createText(cssContent));
                model.add(modelFactory.createCloseElementTag("style"));
                
                // 注入JavaScript代码
                String jsContent = generateJavaScriptContent(setting);
                IProcessableElementTag scriptOpenTag = modelFactory.createOpenElementTag("script");
                model.add(scriptOpenTag);
                model.add(modelFactory.createText(jsContent));
                model.add(modelFactory.createCloseElementTag("script"));
                
                // 注入HTML结构
                String htmlContent = generateHtmlContent(setting);
                IProcessableElementTag htmlScriptOpenTag = modelFactory.createOpenElementTag("script");
                model.add(htmlScriptOpenTag);
                model.add(modelFactory.createText(
                    "document.addEventListener('DOMContentLoaded', function() {" +
                    "  var capsuleHtml = `" + htmlContent + "`;" +
                    "  document.body.insertAdjacentHTML('beforeend', capsuleHtml);" +
                    "});"
                ));
                model.add(modelFactory.createCloseElementTag("script"));
            })
            .then();
    }
    
    private String generateCssContent(JyfacSetting setting) {
        String positionCss = switch (setting.position()) {
            case "bottom-left" -> "bottom: 20px; left: 20px;";
            case "bottom-right" -> "bottom: 20px; right: 20px;";
            case "top-center" -> "top: 20px; left: 50%; transform: translateX(-50%);";
            default -> "bottom: 20px; left: 50%; transform: translateX(-50%);";
        };
        
        return String.format("""
            .capsule-container {
                position: fixed;
                %s
                z-index: %d;
                display: flex;
                align-items: center;
                gap: 15px;
            }
            
            .capsule-bar {
                background: %s;
                border-radius: %dpx;
                padding: 12px 20px;
                display: flex;
                align-items: center;
                gap: 15px;
                box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
                backdrop-filter: blur(10px);
                transition: all 0.3s ease;
                animation: slideIn 0.5s ease;
                position: relative;
                overflow: hidden;
                min-height: 50px;
            }
            
            .capsule-bar:hover {
                transform: translateY(-2px);
                box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
            }
            
            .capsule-bar.closing {
                animation: slideOut 0.3s ease forwards;
            }
            
            @keyframes slideIn {
                from { opacity: 0; transform: translateY(-20px); }
                to { opacity: 1; transform: translateY(0); }
            }
            
            @keyframes slideOut {
                to { opacity: 0; transform: translateX(100px); }
            }
            
            .capsule-content {
                display: flex;
                align-items: center;
                gap: 10px;
                flex: 1;
            }
            
            .capsule-icon {
                width: 24px;
                height: 24px;
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 18px;
                flex-shrink: 0;
            }
            
            .capsule-icon svg, .capsule-icon img {
                width: 100%%;
                height: 100%%;
                animation: swing 2s ease-in-out infinite;
            }
            
            @keyframes swing {
                0%%, 100%% { transform: rotate(0deg); }
                25%% { transform: rotate(-15deg); }
                75%% { transform: rotate(15deg); }
            }
            
            .capsule-text-container {
                flex: 1;
                min-width: 0;
            }
            
            .capsule-text {
                color: %s;
                font-size: 14px;
                font-weight: 500;
                transition: all 0.3s ease;
            }
            
            .capsule-text.single-line {
                height: 20px;
                overflow: hidden;
                position: relative;
                white-space: nowrap;
            }
            
            .capsule-text.multi-line {
                max-height: 60px;
                overflow: hidden;
                position: relative;
            }
            
            .capsule-text.multi-line .text-item {
                opacity: 0;
                transform: translateY(20px);
                transition: all 0.3s ease;
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                white-space: normal;
                line-height: 1.4;
                padding: 8px 0;
            }
            
            .capsule-text.multi-line .text-item.active {
                opacity: 1;
                transform: translateY(0);
                position: relative;
            }
            
            .capsule-text.multi-line .text-item.slide-up {
                opacity: 0;
                transform: translateY(-20px);
            }
            
            .capsule-text.multi-line .text-item.slide-down {
                opacity: 0;
                transform: translateY(20px);
            }
            
            .close-btn {
                width: 24px;
                height: 24px;
                border-radius: 50%%;
                background: #f1f3f5;
                border: none;
                cursor: pointer;
                display: flex;
                align-items: center;
                justify-content: center;
                transition: all 0.2s ease;
                flex-shrink: 0;
            }
            
            .close-btn:hover {
                background: #e9ecef;
                transform: rotate(90deg);
            }
            
            .close-btn:active {
                transform: rotate(90deg) scale(0.9);
            }
            
            .close-btn::before,
            .close-btn::after {
                content: '';
                position: absolute;
                width: 12px;
                height: 2px;
                background: #495057;
                border-radius: 1px;
            }
            
            .close-btn::before {
                transform: rotate(45deg);
            }
            
            .close-btn::after {
                transform: rotate(-45deg);
            }
            """, 
            positionCss, 
            setting.zIndex(), 
            setting.backgroundColor(), 
            setting.borderRadius(), 
            setting.textColor()
        );
    }
    
    private String generateJavaScriptContent(JyfacSetting setting) {
        StringBuilder js = new StringBuilder();
        
        // 基础变量定义
        js.append(String.format("""
            var isMultiLineMode = %s;
            var currentIndex = 0;
            var totalItems = 0;
            var autoScrollTimer = null;
            var autoScrollInterval = %d;
            var isAutoScrollEnabled = %s;
            
            """, 
            "multi".equals(setting.displayMode()) ? "true" : "false",
            setting.scrollInterval() * 1000,
            setting.autoScroll() ? "true" : "false"
        ));
        
        // 多行模式功能函数
        if ("multi".equals(setting.displayMode())) {
            js.append("""
                function startAutoScroll() {
                    if (!isMultiLineMode || totalItems <= 1 || !isAutoScrollEnabled) return;
                    stopAutoScroll();
                    autoScrollTimer = setInterval(function() {
                        if (isAutoScrollEnabled && isMultiLineMode) {
                            var nextIndex = (currentIndex + 1) % totalItems;
                            scrollToItem(nextIndex, 'down');
                        }
                    }, autoScrollInterval);
                }
                
                function stopAutoScroll() {
                    if (autoScrollTimer) {
                        clearInterval(autoScrollTimer);
                        autoScrollTimer = null;
                    }
                }
                
                function scrollToItem(index, direction) {
                    if (index < 0 || index >= totalItems) return;
                    var multiLineText = document.querySelector('.capsule-text.multi-line');
                    if (!multiLineText) return;
                    var textItems = multiLineText.querySelectorAll('.text-item');
                    var currentItem = textItems[currentIndex];
                    var targetItem = textItems[index];
                    
                    currentItem.classList.remove('active');
                    if (direction === 'up') {
                        currentItem.classList.add('slide-down');
                        targetItem.classList.add('slide-up');
                    } else {
                        currentItem.classList.add('slide-up');
                        targetItem.classList.add('slide-down');
                    }
                    
                    setTimeout(function() {
                        textItems.forEach(function(item) {
                            item.classList.remove('active', 'slide-up', 'slide-down');
                        });
                        targetItem.classList.add('active');
                        currentIndex = index;
                    }, 150);
                }
                
                function initMultiLineMode() {
                    var multiLineText = document.querySelector('.capsule-text.multi-line');
                    if (!multiLineText) return;
                    var textItems = multiLineText.querySelectorAll('.text-item');
                    totalItems = textItems.length;
                    currentIndex = 0;
                    
                    textItems.forEach(function(item, index) {
                        item.classList.remove('active', 'slide-up', 'slide-down');
                        if (index === 0) {
                            item.classList.add('active');
                        }
                    });
                    
                    if (totalItems > 1 && isAutoScrollEnabled) {
                        startAutoScroll();
                    }
                }
                
                """);
        }
        
        // 关闭功能
        if (setting.allowClose()) {
            js.append("""
                document.addEventListener('click', function(e) {
                    if (e.target.classList.contains('close-btn') || e.target.parentElement.classList.contains('close-btn')) {
                        var capsule = e.target.closest('.capsule-bar');
                        capsule.classList.add('closing');
                        setTimeout(function() {
                            capsule.remove();
                        }, 300);
                    }
                });
                """);
        }
        
        // 自动隐藏功能
        if (setting.autoHideDelay() > 0) {
            js.append(String.format("""
                setTimeout(function() {
                    var capsule = document.querySelector('.capsule-bar');
                    if (capsule) {
                        capsule.classList.add('closing');
                        setTimeout(function() {
                            capsule.remove();
                        }, 300);
                    }
                }, %d);
                """, setting.autoHideDelay() * 1000));
        }
        
        // 初始化函数
        if ("multi".equals(setting.displayMode())) {
            js.append("""
                document.addEventListener('DOMContentLoaded', function() {
                    setTimeout(function() {
                        initMultiLineMode();
                    }, 100);
                });
                """);
        }
        
        return js.toString();
    }
    
    private String generateHtmlContent(JyfacSetting setting) {
        String iconHtml = getIconContent(setting);
        if (!iconHtml.isEmpty()) {
            iconHtml = "<div class=\"capsule-icon\">" + iconHtml + "</div>";
        }
        
        String closeButtonHtml = setting.allowClose() ? 
            "<button class=\"close-btn\" aria-label=\"关闭\"></button>" : "";
        
        String textContentHtml;
        
        if ("multi".equals(setting.displayMode())) {
            // 多行模式
            StringBuilder multiLineHtml = new StringBuilder();
            multiLineHtml.append("<div class=\"capsule-text-container\">");
            multiLineHtml.append("<div class=\"capsule-text multi-line\">");
            
            String[] lines = setting.multiLineTexts().split("\\n");
            for (int i = 0; i < lines.length; i++) {
                String activeClass = i == 0 ? " active" : "";
                multiLineHtml.append(String.format(
                    "<div class=\"text-item%s\">%s</div>", 
                    activeClass, 
                    lines[i].trim()
                ));
            }
            
            multiLineHtml.append("</div>");
            multiLineHtml.append("</div>");
            textContentHtml = multiLineHtml.toString();
        } else {
            // 单行模式
            textContentHtml = String.format(
                "<div class=\"capsule-text-container\">" +
                "<div class=\"capsule-text single-line\">" +
                "<div class=\"text-item active\">%s</div>" +
                "</div>" +
                "</div>", 
                setting.text()
            );
        }
        
        return String.format("""
            <div class="capsule-container">
                <div class="capsule-bar">
                    <div class="capsule-content">
                        %s
                        %s
                    </div>
                    %s
                </div>
            </div>
            """, iconHtml, textContentHtml, closeButtonHtml);
    }
    
    /**
     * 获取图标内容
     * 根据设置返回SVG或图片标签
     * @param setting 插件设置
     * @return 图标HTML内容
     */
    private String getIconContent(JyfacSetting setting) {
        if (!setting.showIcon()) {
            return "";
        }
        
        return switch (setting.iconType()) {
            case "svg" -> setting.customSvg() != null && !setting.customSvg().trim().isEmpty() 
                ? setting.customSvg() 
                : "<svg t=\"1760148892394\" class=\"icon\" viewBox=\"0 0 1024 1024\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\" p-id=\"6215\" width=\"32\" height=\"32\"><path d=\"M505.152 72.064c65.28 0 120.576 46.08 132.032 106.304 103.808 48.256 169.856 148.48 169.856 260.096v113.088l84.096 116.352c20.352 28.16 23.168 63.424 7.36 94.336a95.552 95.552 0 0 1-85.824 51.584h-139.52a150.656 150.656 0 0 1-138.816 129.792l-10.24 0.384h-22.208a150.656 150.656 0 0 1-149.12-130.176H213.44c-36.032 0-68.608-19.2-85.12-50.176a89.152 89.152 0 0 1 4.672-93.056l70.272-104.96V438.464c0-111.616 66.112-211.84 169.92-260.096 11.456-60.16 66.752-106.24 132.032-106.24z m105.792 741.12H419.328a35.84 35.84 0 0 1-4.096 0.512 89.152 89.152 0 0 0 86.656 68.864h22.272c42.304 0 77.824-29.76 86.784-69.376z m28.8-60.8h-2.56c-11.328 0-3.2 11.072 24.32 33.152-27.52 18.88-35.648 28.288-24.32 28.288h35.904l1.088-9.984c0.192-3.392 0.32-6.848 0.32-10.24a41.92 41.92 0 0 0-29.76-40.128l-5.056-1.152z m-134.528-618.88c-39.36 0-72 29.888-72.768 66.624a30.72 30.72 0 0 1-19.456 27.968c-90.048 35.456-148.288 118.08-148.288 210.368v136.576a30.784 30.784 0 0 1-5.12 17.088l-75.52 112.768a28.48 28.48 0 0 0-1.6 29.888 34.112 34.112 0 0 0 30.912 17.664H385.92a39.04 39.04 0 0 1 7.424-0.704h239.36c2.048 0 4.16 0.128 6.144 0.448l0.832 0.128 172.992 0.064a34.56 34.56 0 0 0 28.288-13.568l2.88-4.608a28.224 28.224 0 0 0-2.496-30.336l-89.856-124.416a30.656 30.656 0 0 1-5.76-17.92V438.4c0-92.288-58.24-174.912-148.352-210.368a30.72 30.72 0 0 1-19.456-27.968c-0.768-36.736-33.408-66.624-72.704-66.624zM416.512 340.416a30.528 30.528 0 0 1-1.6 34.752c-15.04 19.84-35.2 61.44-22.336 134.144a31.552 31.552 0 0 1-17.856 34.944 30.72 30.72 0 0 1-42.368-22.784c-17.088-94.848 9.792-152.896 33.92-183.936a30.72 30.72 0 0 1 50.24 2.88z\" fill=\"#505766\" p-id=\"6216\"></path><path d=\"M505.152 59.264c70.656 0 131.84 49.792 144.64 116.736l-1.536-6.4 7.68 3.776c96.448 50.304 158.4 144.896 163.584 250.624l0.32 14.464v108.928l81.664 113.024a101.76 101.76 0 0 1 12.48 98.752l-4.096 8.896a108.352 108.352 0 0 1-97.28 58.56l-128.768-0.128-2.432 10.368a163.712 163.712 0 0 1-135.68 118.4l-10.88 1.152-10.752 0.384h-22.208a163.52 163.52 0 0 1-157.632-120.768l-2.24-9.472-128.64 0.064a108.992 108.992 0 0 1-91.392-48.448l-5.12-8.512a101.952 101.952 0 0 1 5.44-106.176l68.032-101.76 0.064-123.264c0-111.424 63.104-212.48 163.968-265.088l7.552-3.84 0.896-3.456C378.56 107.456 431.936 64 494.656 59.648l10.496-0.384z m0 25.6c-58.496 0-109.056 41.152-119.488 95.936l-7.168 9.216C278.848 236.288 216 332.16 216 438.4v127.168l-2.176 7.168-70.272 104.96a76.416 76.416 0 0 0-4.032 79.872c14.272 26.752 42.496 43.392 73.856 43.392h150.656l1.472 11.072a137.856 137.856 0 0 0 136.32 119.04h21.824l9.856-0.256a137.92 137.92 0 0 0 122.432-97.92l1.536-6.4-20.288 0.064c-7.04 0-12.032-2.56-14.912-6.4l-1.28 4.8a102.08 102.08 0 0 1-87.424 69.888l-9.408 0.448h-22.272a101.952 101.952 0 0 1-99.136-78.72l-3.328-14.528 14.784-1.152 5.12-0.64h204.288l1.472-1.92c3.008-3.52 7.68-7.616 14.08-12.544l1.472-1.216-2.24-1.856a136.704 136.704 0 0 1-10.432-10.24l-3.456-4.288-2.368-3.776H393.344a33.088 33.088 0 0 0-4.032 0.32l-3.392 0.448H213.376a46.912 46.912 0 0 1-42.24-24.448 40.192 40.192 0 0 1 2.304-43.072l75.456-112.64c1.92-3.008 3.008-6.464 3.008-10.048V438.4c0-97.664 61.44-184.896 156.352-222.272a17.92 17.92 0 0 0 11.392-16.32c0.896-43.904 39.424-79.168 85.568-79.168 46.08 0 84.608 35.2 85.504 79.168a17.92 17.92 0 0 0 11.392 16.32c94.848 37.376 156.352 124.608 156.352 222.272v123.072c0 3.712 1.216 7.36 3.456 10.432l89.792 124.416a40.32 40.32 0 0 1 3.008 44.544l-3.456 5.504a47.36 47.36 0 0 1-38.592 18.752l-133.568-0.256 1.088 1.728a54.4 54.4 0 0 1 6.656 19.712l0.448 7.168c0 3.776-0.128 7.488-0.384 11.648l0.128-4.224 125.632 0.064c28.736 0 54.656-13.824 69.76-36.672l4.672-7.936a76.288 76.288 0 0 0-6.4-81.024l-84.032-116.352-2.432-7.488V438.4c0-106.24-62.784-202.176-162.432-248.448l-7.168-9.216c-10.496-54.784-60.992-95.936-119.488-95.936z m-72.32 740.8c12.416 26.368 39.04 44.096 69.12 44.096h22.208c27.52 0 52.032-14.784 65.472-37.312l3.392-6.528-160.192-0.256z m72.32-679.36c-32.512 0-59.328 24.512-59.904 54.08a43.52 43.52 0 0 1-27.52 39.68c-85.312 33.536-140.224 111.424-140.224 198.4v136.576c0 8.576-2.56 17.024-7.36 24.192l-75.456 112.768a15.296 15.296 0 0 0-0.896 16.768c3.584 6.784 10.752 10.88 19.584 10.88l170.176 0.192a51.84 51.84 0 0 1 9.792-0.896h239.36c2.688 0 5.376 0.192 8.704 0.64h-0.576 171.84c6.144 0 11.52-1.92 15.104-5.056l2.304-2.496 2.368-3.776a15.104 15.104 0 0 0-1.472-16.832l-89.792-124.416a43.456 43.456 0 0 1-8.32-25.472V438.4c0-86.976-54.912-164.864-140.16-198.464a43.52 43.52 0 0 1-27.52-39.616c-0.64-29.568-27.456-54.08-59.968-54.08zM356.224 329.6a43.52 43.52 0 0 1 71.232 4.032 43.328 43.328 0 0 1-2.304 49.216c-20.224 26.688-30.016 67.008-19.84 124.16a44.288 44.288 0 0 1-25.536 48.96 43.52 43.52 0 0 1-59.904-32.32c-15.232-84.16 0.896-148.352 36.352-194.048z m20.224 15.68c-31.04 40-45.312 96.896-31.36 173.824a17.92 17.92 0 0 0 24.704 13.312c7.552-3.2 11.904-11.904 10.24-20.928-11.328-64.192 0.192-111.808 24.768-144.192a17.728 17.728 0 0 0 0.896-20.16 17.92 17.92 0 0 0-29.248-1.856z\" fill=\"#505766\" p-id=\"6217\"></path></svg>";
            case "image" -> {
                String imageUrl = setting.imageUrl() != null && !setting.imageUrl().trim().isEmpty() 
                    ? setting.imageUrl() 
                    : "/plugins/jyfac/assets/static/tz.svg";
                yield "<img src=\"" + imageUrl + "\" alt=\"图标\" width=\"32\" height=\"32\">";
            }
            default -> "";
        };
    }
}