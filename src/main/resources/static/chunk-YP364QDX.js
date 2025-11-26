import{A as ut,Ba as vt,D as a,G as Q,Ga as f,I as ht,Ja as yt,K as P,Na as Ot,R as u,W as gt,e as lt,f as at,fb as q,g as pt,hb as wt,ia as bt,sa as ft,ta as F,v as T,va as V,xa as mt,y as ct,z as dt}from"./chunk-WN6J673O.js";import{$a as l,Ab as j,Db as it,Eb as O,Hb as D,Jb as B,Kb as M,Na as y,Pa as K,Pb as w,Qb as nt,R as G,Rb as st,S as R,Sa as H,T as L,Ua as Y,X as E,Ya as p,Yb as I,Za as x,_a as J,_b as W,a as v,ab as X,bb as z,ca as g,db as tt,eb as et,kc as rt,lb as C,mb as A,nb as k,nc as _,oa as $,pa as r,rb as N,sb as U,sc as c,tb as Z,tc as d,ub as ot,xb as b}from"./chunk-WF3CSDPJ.js";var Tt=class{};var Rt=class{};var Lt=class{};var Et=class{};var xt=class{};var Ct=class{};var _t=["data-p-icon","chevron-right"],oe=(()=>{class e extends f{static \u0275fac=(()=>{let t;return function(i){return(t||(t=r(e)))(i||e)}})();static \u0275cmp=p({type:e,selectors:[["","data-p-icon","chevron-right"]],features:[l],attrs:_t,decls:1,vars:0,consts:[["d","M4.38708 13C4.28408 13.0005 4.18203 12.9804 4.08691 12.9409C3.99178 12.9014 3.9055 12.8433 3.83313 12.7701C3.68634 12.6231 3.60388 12.4238 3.60388 12.2161C3.60388 12.0084 3.68634 11.8091 3.83313 11.6622L8.50507 6.99022L3.83313 2.31827C3.69467 2.16968 3.61928 1.97313 3.62287 1.77005C3.62645 1.56698 3.70872 1.37322 3.85234 1.22959C3.99596 1.08597 4.18972 1.00371 4.3928 1.00012C4.59588 0.996539 4.79242 1.07192 4.94102 1.21039L10.1669 6.43628C10.3137 6.58325 10.3962 6.78249 10.3962 6.99022C10.3962 7.19795 10.3137 7.39718 10.1669 7.54416L4.94102 12.7701C4.86865 12.8433 4.78237 12.9014 4.68724 12.9409C4.59212 12.9804 4.49007 13.0005 4.38708 13Z","fill","currentColor"]],template:function(o,i){o&1&&(g(),b(0,"path",0))},encapsulation:2})}return e})();var At=`
    .p-tooltip {
        position: absolute;
        display: none;
        max-width: dt('tooltip.max.width');
    }

    .p-tooltip-right,
    .p-tooltip-left {
        padding: 0 dt('tooltip.gutter');
    }

    .p-tooltip-top,
    .p-tooltip-bottom {
        padding: dt('tooltip.gutter') 0;
    }

    .p-tooltip-text {
        white-space: pre-line;
        word-break: break-word;
        background: dt('tooltip.background');
        color: dt('tooltip.color');
        padding: dt('tooltip.padding');
        box-shadow: dt('tooltip.shadow');
        border-radius: dt('tooltip.border.radius');
    }

    .p-tooltip-arrow {
        position: absolute;
        width: 0;
        height: 0;
        border-color: transparent;
        border-style: solid;
    }

    .p-tooltip-right .p-tooltip-arrow {
        margin-top: calc(-1 * dt('tooltip.gutter'));
        border-width: dt('tooltip.gutter') dt('tooltip.gutter') dt('tooltip.gutter') 0;
        border-right-color: dt('tooltip.background');
    }

    .p-tooltip-left .p-tooltip-arrow {
        margin-top: calc(-1 * dt('tooltip.gutter'));
        border-width: dt('tooltip.gutter') 0 dt('tooltip.gutter') dt('tooltip.gutter');
        border-left-color: dt('tooltip.background');
    }

    .p-tooltip-top .p-tooltip-arrow {
        margin-left: calc(-1 * dt('tooltip.gutter'));
        border-width: dt('tooltip.gutter') dt('tooltip.gutter') 0 dt('tooltip.gutter');
        border-top-color: dt('tooltip.background');
        border-bottom-color: dt('tooltip.background');
    }

    .p-tooltip-bottom .p-tooltip-arrow {
        margin-left: calc(-1 * dt('tooltip.gutter'));
        border-width: 0 dt('tooltip.gutter') dt('tooltip.gutter') dt('tooltip.gutter');
        border-top-color: dt('tooltip.background');
        border-bottom-color: dt('tooltip.background');
    }
`;var Pt={root:"p-tooltip p-component",arrow:"p-tooltip-arrow",text:"p-tooltip-text"},kt=(()=>{class e extends V{name="tooltip";theme=At;classes=Pt;static \u0275fac=(()=>{let t;return function(i){return(t||(t=r(e)))(i||e)}})();static \u0275prov=R({token:e,factory:e.\u0275fac})}return e})();var me=(()=>{class e extends mt{zone;viewContainer;tooltipPosition;tooltipEvent="hover";positionStyle;tooltipStyleClass;tooltipZIndex;escape=!0;showDelay;hideDelay;life;positionTop;positionLeft;autoHide=!0;fitContent=!0;hideOnEscape=!0;content;get disabled(){return this._disabled}set disabled(t){this._disabled=t,this.deactivate()}tooltipOptions;appendTo=_(void 0);$appendTo=rt(()=>this.appendTo()||this.config.overlayAppendTo());_tooltipOptions={tooltipLabel:null,tooltipPosition:"right",tooltipEvent:"hover",appendTo:"body",positionStyle:null,tooltipStyleClass:null,tooltipZIndex:"auto",escape:!0,disabled:null,showDelay:null,hideDelay:null,positionTop:null,positionLeft:null,life:null,autoHide:!0,hideOnEscape:!0,id:bt("pn_id_")+"_tooltip"};_disabled;container;styleClass;tooltipText;showTimeout;hideTimeout;active;mouseEnterListener;mouseLeaveListener;containerMouseleaveListener;clickListener;focusListener;blurListener;documentEscapeListener;scrollHandler;resizeListener;_componentStyle=E(kt);interactionInProgress=!1;constructor(t,o){super(),this.zone=t,this.viewContainer=o}ngAfterViewInit(){super.ngAfterViewInit(),pt(this.platformId)&&this.zone.runOutsideAngular(()=>{let t=this.getOption("tooltipEvent");if((t==="hover"||t==="both")&&(this.mouseEnterListener=this.onMouseEnter.bind(this),this.mouseLeaveListener=this.onMouseLeave.bind(this),this.clickListener=this.onInputClick.bind(this),this.el.nativeElement.addEventListener("mouseenter",this.mouseEnterListener),this.el.nativeElement.addEventListener("click",this.clickListener),this.el.nativeElement.addEventListener("mouseleave",this.mouseLeaveListener)),t==="focus"||t==="both"){this.focusListener=this.onFocus.bind(this),this.blurListener=this.onBlur.bind(this);let o=this.el.nativeElement.querySelector(".p-component");o||(o=this.getTarget(this.el.nativeElement)),o.addEventListener("focus",this.focusListener),o.addEventListener("blur",this.blurListener)}})}ngOnChanges(t){super.ngOnChanges(t),t.tooltipPosition&&this.setOption({tooltipPosition:t.tooltipPosition.currentValue}),t.tooltipEvent&&this.setOption({tooltipEvent:t.tooltipEvent.currentValue}),t.appendTo&&this.setOption({appendTo:t.appendTo.currentValue}),t.positionStyle&&this.setOption({positionStyle:t.positionStyle.currentValue}),t.tooltipStyleClass&&this.setOption({tooltipStyleClass:t.tooltipStyleClass.currentValue}),t.tooltipZIndex&&this.setOption({tooltipZIndex:t.tooltipZIndex.currentValue}),t.escape&&this.setOption({escape:t.escape.currentValue}),t.showDelay&&this.setOption({showDelay:t.showDelay.currentValue}),t.hideDelay&&this.setOption({hideDelay:t.hideDelay.currentValue}),t.life&&this.setOption({life:t.life.currentValue}),t.positionTop&&this.setOption({positionTop:t.positionTop.currentValue}),t.positionLeft&&this.setOption({positionLeft:t.positionLeft.currentValue}),t.disabled&&this.setOption({disabled:t.disabled.currentValue}),t.content&&(this.setOption({tooltipLabel:t.content.currentValue}),this.active&&(t.content.currentValue?this.container&&this.container.offsetParent?(this.updateText(),this.align()):this.show():this.hide())),t.autoHide&&this.setOption({autoHide:t.autoHide.currentValue}),t.id&&this.setOption({id:t.id.currentValue}),t.tooltipOptions&&(this._tooltipOptions=v(v({},this._tooltipOptions),t.tooltipOptions.currentValue),this.deactivate(),this.active&&(this.getOption("tooltipLabel")?this.container&&this.container.offsetParent?(this.updateText(),this.align()):this.show():this.hide()))}isAutoHide(){return this.getOption("autoHide")}onMouseEnter(t){!this.container&&!this.showTimeout&&this.activate()}onMouseLeave(t){this.isAutoHide()?this.deactivate():!(T(t.relatedTarget,"p-tooltip")||T(t.relatedTarget,"p-tooltip-text")||T(t.relatedTarget,"p-tooltip-arrow"))&&this.deactivate()}onFocus(t){this.activate()}onBlur(t){this.deactivate()}onInputClick(t){this.deactivate()}activate(){if(!this.interactionInProgress){if(this.active=!0,this.clearHideTimeout(),this.getOption("showDelay")?this.showTimeout=setTimeout(()=>{this.show()},this.getOption("showDelay")):this.show(),this.getOption("life")){let t=this.getOption("showDelay")?this.getOption("life")+this.getOption("showDelay"):this.getOption("life");this.hideTimeout=setTimeout(()=>{this.hide()},t)}this.getOption("hideOnEscape")&&(this.documentEscapeListener=this.renderer.listen("document","keydown.escape",()=>{this.deactivate(),this.documentEscapeListener()})),this.interactionInProgress=!0}}deactivate(){this.interactionInProgress=!1,this.active=!1,this.clearShowTimeout(),this.getOption("hideDelay")?(this.clearHideTimeout(),this.hideTimeout=setTimeout(()=>{this.hide()},this.getOption("hideDelay"))):this.hide(),this.documentEscapeListener&&this.documentEscapeListener()}create(){this.container&&(this.clearHideTimeout(),this.remove()),this.container=document.createElement("div"),this.container.setAttribute("id",this.getOption("id")),this.container.setAttribute("role","tooltip");let t=document.createElement("div");t.className="p-tooltip-arrow",t.setAttribute("data-pc-section","arrow"),this.container.appendChild(t),this.tooltipText=document.createElement("div"),this.tooltipText.className="p-tooltip-text",this.updateText(),this.getOption("positionStyle")&&(this.container.style.position=this.getOption("positionStyle")),this.container.appendChild(this.tooltipText),this.getOption("appendTo")==="body"?document.body.appendChild(this.container):this.getOption("appendTo")==="target"?Q(this.container,this.el.nativeElement):Q(this.getOption("appendTo"),this.container),this.container.style.display="none",this.fitContent&&(this.container.style.width="fit-content"),this.isAutoHide()?this.container.style.pointerEvents="none":(this.container.style.pointerEvents="unset",this.bindContainerMouseleaveListener())}bindContainerMouseleaveListener(){if(!this.containerMouseleaveListener){let t=this.container??this.container.nativeElement;this.containerMouseleaveListener=this.renderer.listen(t,"mouseleave",o=>{this.deactivate()})}}unbindContainerMouseleaveListener(){this.containerMouseleaveListener&&(this.bindContainerMouseleaveListener(),this.containerMouseleaveListener=null)}show(){if(!this.getOption("tooltipLabel")||this.getOption("disabled"))return;this.create(),this.el.nativeElement.closest("p-dialog")?setTimeout(()=>{this.container&&(this.container.style.display="inline-block"),this.container&&this.align()},100):(this.container.style.display="inline-block",this.align()),ht(this.container,250),this.getOption("tooltipZIndex")==="auto"?q.set("tooltip",this.container,this.config.zIndex.tooltip):this.container.style.zIndex=this.getOption("tooltipZIndex"),this.bindDocumentResizeListener(),this.bindScrollListener()}hide(){this.getOption("tooltipZIndex")==="auto"&&q.clear(this.container),this.remove()}updateText(){let t=this.getOption("tooltipLabel");if(t instanceof K){let o=this.viewContainer.createEmbeddedView(t);o.detectChanges(),o.rootNodes.forEach(i=>this.tooltipText.appendChild(i))}else this.getOption("escape")?(this.tooltipText.innerHTML="",this.tooltipText.appendChild(document.createTextNode(t))):this.tooltipText.innerHTML=t}align(){let t=this.getOption("tooltipPosition"),o={top:[this.alignTop,this.alignBottom,this.alignRight,this.alignLeft],bottom:[this.alignBottom,this.alignTop,this.alignRight,this.alignLeft],left:[this.alignLeft,this.alignRight,this.alignTop,this.alignBottom],right:[this.alignRight,this.alignLeft,this.alignTop,this.alignBottom]};for(let[i,n]of o[t].entries())if(i===0)n.call(this);else if(this.isOutOfBounds())n.call(this);else break}getHostOffset(){if(this.getOption("appendTo")==="body"||this.getOption("appendTo")==="target"){let t=this.el.nativeElement.getBoundingClientRect(),o=t.left+dt(),i=t.top+ut();return{left:o,top:i}}else return{left:0,top:0}}get activeElement(){return this.el.nativeElement.nodeName.startsWith("P-")?P(this.el.nativeElement,".p-component"):this.el.nativeElement}alignRight(){this.preAlign("right");let t=this.activeElement,o=a(t),i=(u(t)-u(this.container))/2;this.alignTooltip(o,i);let n=this.getArrowElement();n.style.top="50%",n.style.right=null,n.style.bottom=null,n.style.left="0"}alignLeft(){this.preAlign("left");let t=this.getArrowElement(),o=a(this.container),i=(u(this.el.nativeElement)-u(this.container))/2;this.alignTooltip(-o,i),t.style.top="50%",t.style.right="0",t.style.bottom=null,t.style.left=null}alignTop(){this.preAlign("top");let t=this.getArrowElement(),o=this.getHostOffset(),i=a(this.container),n=(a(this.el.nativeElement)-a(this.container))/2,s=u(this.container);this.alignTooltip(n,-s);let h=o.left-this.getHostOffset().left+i/2;t.style.top=null,t.style.right=null,t.style.bottom="0",t.style.left=h+"px"}getArrowElement(){return P(this.container,'[data-pc-section="arrow"]')}alignBottom(){this.preAlign("bottom");let t=this.getArrowElement(),o=a(this.container),i=this.getHostOffset(),n=(a(this.el.nativeElement)-a(this.container))/2,s=u(this.el.nativeElement);this.alignTooltip(n,s);let h=i.left-this.getHostOffset().left+o/2;t.style.top="0",t.style.right=null,t.style.bottom=null,t.style.left=h+"px"}alignTooltip(t,o){let i=this.getHostOffset(),n=i.left+t,s=i.top+o;this.container.style.left=n+this.getOption("positionLeft")+"px",this.container.style.top=s+this.getOption("positionTop")+"px"}setOption(t){this._tooltipOptions=v(v({},this._tooltipOptions),t)}getOption(t){return this._tooltipOptions[t]}getTarget(t){return T(t,"p-inputwrapper")?P(t,"input"):t}preAlign(t){this.container.style.left="-999px",this.container.style.top="-999px";let o="p-tooltip p-component p-tooltip-"+t;this.container.className=this.getOption("tooltipStyleClass")?o+" "+this.getOption("tooltipStyleClass"):o}isOutOfBounds(){let t=this.container.getBoundingClientRect(),o=t.top,i=t.left,n=a(this.container),s=u(this.container),h=ct();return i+n>h.width||i<0||o<0||o+s>h.height}onWindowResize(t){this.hide()}bindDocumentResizeListener(){this.zone.runOutsideAngular(()=>{this.resizeListener=this.onWindowResize.bind(this),window.addEventListener("resize",this.resizeListener)})}unbindDocumentResizeListener(){this.resizeListener&&(window.removeEventListener("resize",this.resizeListener),this.resizeListener=null)}bindScrollListener(){this.scrollHandler||(this.scrollHandler=new vt(this.el.nativeElement,()=>{this.container&&this.hide()})),this.scrollHandler.bindScrollListener()}unbindScrollListener(){this.scrollHandler&&this.scrollHandler.unbindScrollListener()}unbindEvents(){let t=this.getOption("tooltipEvent");if((t==="hover"||t==="both")&&(this.el.nativeElement.removeEventListener("mouseenter",this.mouseEnterListener),this.el.nativeElement.removeEventListener("mouseleave",this.mouseLeaveListener),this.el.nativeElement.removeEventListener("click",this.clickListener)),t==="focus"||t==="both"){let o=this.el.nativeElement.querySelector(".p-component");o||(o=this.getTarget(this.el.nativeElement)),o.removeEventListener("focus",this.focusListener),o.removeEventListener("blur",this.blurListener)}this.unbindDocumentResizeListener()}remove(){this.container&&this.container.parentElement&&(this.getOption("appendTo")==="body"?document.body.removeChild(this.container):this.getOption("appendTo")==="target"?this.el.nativeElement.removeChild(this.container):gt(this.getOption("appendTo"),this.container)),this.unbindDocumentResizeListener(),this.unbindScrollListener(),this.unbindContainerMouseleaveListener(),this.clearTimeouts(),this.container=null,this.scrollHandler=null}clearShowTimeout(){this.showTimeout&&(clearTimeout(this.showTimeout),this.showTimeout=null)}clearHideTimeout(){this.hideTimeout&&(clearTimeout(this.hideTimeout),this.hideTimeout=null)}clearTimeouts(){this.clearShowTimeout(),this.clearHideTimeout()}ngOnDestroy(){this.unbindEvents(),super.ngOnDestroy(),this.container&&q.clear(this.container),this.remove(),this.scrollHandler&&(this.scrollHandler.destroy(),this.scrollHandler=null),this.documentEscapeListener&&this.documentEscapeListener()}static \u0275fac=function(o){return new(o||e)(H(et),H(Y))};static \u0275dir=J({type:e,selectors:[["","pTooltip",""]],inputs:{tooltipPosition:"tooltipPosition",tooltipEvent:"tooltipEvent",positionStyle:"positionStyle",tooltipStyleClass:"tooltipStyleClass",tooltipZIndex:"tooltipZIndex",escape:[2,"escape","escape",c],showDelay:[2,"showDelay","showDelay",d],hideDelay:[2,"hideDelay","hideDelay",d],life:[2,"life","life",d],positionTop:[2,"positionTop","positionTop",d],positionLeft:[2,"positionLeft","positionLeft",d],autoHide:[2,"autoHide","autoHide",c],fitContent:[2,"fitContent","fitContent",c],hideOnEscape:[2,"hideOnEscape","hideOnEscape",c],content:[0,"pTooltip","content"],disabled:[0,"tooltipDisabled","disabled"],tooltipOptions:"tooltipOptions",appendTo:[1,"appendTo"]},features:[I([kt]),l,$]})}return e})(),ve=(()=>{class e{static \u0275fac=function(o){return new(o||e)};static \u0275mod=x({type:e});static \u0275inj=L({})}return e})();var Ft=["data-p-icon","angle-down"],Te=(()=>{class e extends f{static \u0275fac=(()=>{let t;return function(i){return(t||(t=r(e)))(i||e)}})();static \u0275cmp=p({type:e,selectors:[["","data-p-icon","angle-down"]],features:[l],attrs:Ft,decls:1,vars:0,consts:[["d","M3.58659 4.5007C3.68513 4.50023 3.78277 4.51945 3.87379 4.55723C3.9648 4.59501 4.04735 4.65058 4.11659 4.7207L7.11659 7.7207L10.1166 4.7207C10.2619 4.65055 10.4259 4.62911 10.5843 4.65956C10.7427 4.69002 10.8871 4.77074 10.996 4.88976C11.1049 5.00877 11.1726 5.15973 11.1889 5.32022C11.2052 5.48072 11.1693 5.6422 11.0866 5.7807L7.58659 9.2807C7.44597 9.42115 7.25534 9.50004 7.05659 9.50004C6.85784 9.50004 6.66722 9.42115 6.52659 9.2807L3.02659 5.7807C2.88614 5.64007 2.80725 5.44945 2.80725 5.2507C2.80725 5.05195 2.88614 4.86132 3.02659 4.7207C3.09932 4.64685 3.18675 4.58911 3.28322 4.55121C3.37969 4.51331 3.48305 4.4961 3.58659 4.5007Z","fill","currentColor"]],template:function(o,i){o&1&&(g(),b(0,"path",0))},encapsulation:2})}return e})();var Dt=`
    .p-togglebutton {
        display: inline-flex;
        cursor: pointer;
        user-select: none;
        overflow: hidden;
        position: relative;
        color: dt('togglebutton.color');
        background: dt('togglebutton.background');
        border: 1px solid dt('togglebutton.border.color');
        padding: dt('togglebutton.padding');
        font-size: 1rem;
        font-family: inherit;
        font-feature-settings: inherit;
        transition:
            background dt('togglebutton.transition.duration'),
            color dt('togglebutton.transition.duration'),
            border-color dt('togglebutton.transition.duration'),
            outline-color dt('togglebutton.transition.duration'),
            box-shadow dt('togglebutton.transition.duration');
        border-radius: dt('togglebutton.border.radius');
        outline-color: transparent;
        font-weight: dt('togglebutton.font.weight');
    }

    .p-togglebutton-content {
        display: inline-flex;
        flex: 1 1 auto;
        align-items: center;
        justify-content: center;
        gap: dt('togglebutton.gap');
        padding: dt('togglebutton.content.padding');
        background: transparent;
        border-radius: dt('togglebutton.content.border.radius');
        transition:
            background dt('togglebutton.transition.duration'),
            color dt('togglebutton.transition.duration'),
            border-color dt('togglebutton.transition.duration'),
            outline-color dt('togglebutton.transition.duration'),
            box-shadow dt('togglebutton.transition.duration');
    }

    .p-togglebutton:not(:disabled):not(.p-togglebutton-checked):hover {
        background: dt('togglebutton.hover.background');
        color: dt('togglebutton.hover.color');
    }

    .p-togglebutton.p-togglebutton-checked {
        background: dt('togglebutton.checked.background');
        border-color: dt('togglebutton.checked.border.color');
        color: dt('togglebutton.checked.color');
    }

    .p-togglebutton-checked .p-togglebutton-content {
        background: dt('togglebutton.content.checked.background');
        box-shadow: dt('togglebutton.content.checked.shadow');
    }

    .p-togglebutton:focus-visible {
        box-shadow: dt('togglebutton.focus.ring.shadow');
        outline: dt('togglebutton.focus.ring.width') dt('togglebutton.focus.ring.style') dt('togglebutton.focus.ring.color');
        outline-offset: dt('togglebutton.focus.ring.offset');
    }

    .p-togglebutton.p-invalid {
        border-color: dt('togglebutton.invalid.border.color');
    }

    .p-togglebutton:disabled {
        opacity: 1;
        cursor: default;
        background: dt('togglebutton.disabled.background');
        border-color: dt('togglebutton.disabled.border.color');
        color: dt('togglebutton.disabled.color');
    }

    .p-togglebutton-label,
    .p-togglebutton-icon {
        position: relative;
        transition: none;
    }

    .p-togglebutton-icon {
        color: dt('togglebutton.icon.color');
    }

    .p-togglebutton:not(:disabled):not(.p-togglebutton-checked):hover .p-togglebutton-icon {
        color: dt('togglebutton.icon.hover.color');
    }

    .p-togglebutton.p-togglebutton-checked .p-togglebutton-icon {
        color: dt('togglebutton.icon.checked.color');
    }

    .p-togglebutton:disabled .p-togglebutton-icon {
        color: dt('togglebutton.icon.disabled.color');
    }

    .p-togglebutton-sm {
        padding: dt('togglebutton.sm.padding');
        font-size: dt('togglebutton.sm.font.size');
    }

    .p-togglebutton-sm .p-togglebutton-content {
        padding: dt('togglebutton.content.sm.padding');
    }

    .p-togglebutton-lg {
        padding: dt('togglebutton.lg.padding');
        font-size: dt('togglebutton.lg.font.size');
    }

    .p-togglebutton-lg .p-togglebutton-content {
        padding: dt('togglebutton.content.lg.padding');
    }

    .p-togglebutton-fluid {
        width: 100%;
    }
`;var Vt=["icon"],qt=["content"],Mt=e=>({$implicit:e});function St(e,m){e&1&&j(0)}function Ht(e,m){if(e&1&&ot(0,"span"),e&2){let t=O(3);w(t.cn(t.cx("icon"),t.checked?t.onIcon:t.offIcon,t.iconPos==="left"?t.cx("iconLeft"):t.cx("iconRight"))),C("data-pc-section","icon")}}function zt(e,m){if(e&1&&A(0,Ht,1,3,"span",1),e&2){let t=O(2);k(t.onIcon||t.offIcon?0:-1)}}function Nt(e,m){e&1&&j(0)}function Ut(e,m){if(e&1&&z(0,Nt,1,0,"ng-container",0),e&2){let t=O(2);N("ngTemplateOutlet",t.iconTemplate||t._iconTemplate)("ngTemplateOutletContext",W(2,Mt,t.checked))}}function Zt(e,m){if(e&1&&(A(0,zt,1,1)(1,Ut,1,4,"ng-container"),U(2,"span"),nt(3),Z()),e&2){let t=O();k(t.iconTemplate?1:0),y(2),w(t.cx("label")),C("data-pc-section","label"),y(),st(t.checked?t.hasOnLabel?t.onLabel:"\xA0":t.hasOffLabel?t.offLabel:"\xA0")}}var jt=`
    ${Dt}

    /* For PrimeNG (iconPos) */
    .p-togglebutton-icon-right {
        order: 1;
    }

    .p-togglebutton.ng-invalid.ng-dirty {
        border-color: dt('togglebutton.invalid.border.color');
    }
`,Wt={root:({instance:e})=>["p-togglebutton p-component",{"p-togglebutton-checked":e.checked,"p-invalid":e.invalid(),"p-disabled":e.$disabled(),"p-togglebutton-sm p-inputfield-sm":e.size==="small","p-togglebutton-lg p-inputfield-lg":e.size==="large","p-togglebutton-fluid":e.fluid()}],content:"p-togglebutton-content",icon:"p-togglebutton-icon",iconLeft:"p-togglebutton-icon-left",iconRight:"p-togglebutton-icon-right",label:"p-togglebutton-label"},Bt=(()=>{class e extends V{name="togglebutton";theme=jt;classes=Wt;static \u0275fac=(()=>{let t;return function(i){return(t||(t=r(e)))(i||e)}})();static \u0275prov=R({token:e,factory:e.\u0275fac})}return e})();var Qt={provide:Ot,useExisting:G(()=>It),multi:!0},It=(()=>{class e extends wt{onKeyDown(t){switch(t.code){case"Enter":this.toggle(t),t.preventDefault();break;case"Space":this.toggle(t),t.preventDefault();break}}toggle(t){!this.$disabled()&&!(this.allowEmpty===!1&&this.checked)&&(this.checked=!this.checked,this.writeModelValue(this.checked),this.onModelChange(this.checked),this.onModelTouched(),this.onChange.emit({originalEvent:t,checked:this.checked}),this.cd.markForCheck())}onLabel="Yes";offLabel="No";onIcon;offIcon;ariaLabel;ariaLabelledBy;styleClass;inputId;tabindex=0;iconPos="left";autofocus;size;allowEmpty;fluid=_(void 0,{transform:c});onChange=new tt;iconTemplate;contentTemplate;templates;checked=!1;_componentStyle=E(Bt);onBlur(){this.onModelTouched()}get hasOnLabel(){return this.onLabel&&this.onLabel.length>0}get hasOffLabel(){return this.onLabel&&this.onLabel.length>0}get active(){return this.checked===!0}_iconTemplate;_contentTemplate;ngAfterContentInit(){this.templates.forEach(t=>{switch(t.getType()){case"icon":this._iconTemplate=t.template;break;case"content":this._contentTemplate=t.template;break;default:this._contentTemplate=t.template;break}})}writeControlValue(t,o){this.checked=t,o(t),this.cd.markForCheck()}static \u0275fac=(()=>{let t;return function(i){return(t||(t=r(e)))(i||e)}})();static \u0275cmp=p({type:e,selectors:[["p-toggleButton"],["p-togglebutton"],["p-toggle-button"]],contentQueries:function(o,i,n){if(o&1&&(D(n,Vt,4),D(n,qt,4),D(n,ft,4)),o&2){let s;B(s=M())&&(i.iconTemplate=s.first),B(s=M())&&(i.contentTemplate=s.first),B(s=M())&&(i.templates=s)}},hostVars:6,hostBindings:function(o,i){o&1&&it("keydown",function(s){return i.onKeyDown(s)})("click",function(s){return i.toggle(s)}),o&2&&(C("aria-labelledby",i.ariaLabelledBy)("aria-pressed",i.checked)("role","button")("tabindex",i.$disabled()?-1:0),w(i.cn(i.cx("root"),i.styleClass)))},inputs:{onLabel:"onLabel",offLabel:"offLabel",onIcon:"onIcon",offIcon:"offIcon",ariaLabel:"ariaLabel",ariaLabelledBy:"ariaLabelledBy",styleClass:"styleClass",inputId:"inputId",tabindex:[2,"tabindex","tabindex",d],iconPos:"iconPos",autofocus:[2,"autofocus","autofocus",c],size:"size",allowEmpty:"allowEmpty",fluid:[1,"fluid"]},outputs:{onChange:"onChange"},features:[I([Qt,Bt]),X([yt]),l],decls:3,vars:7,consts:[[4,"ngTemplateOutlet","ngTemplateOutletContext"],[3,"class"]],template:function(o,i){o&1&&(U(0,"span"),z(1,St,1,0,"ng-container",0),A(2,Zt,4,5),Z()),o&2&&(w(i.cx("content")),y(),N("ngTemplateOutlet",i.contentTemplate||i._contentTemplate)("ngTemplateOutletContext",W(5,Mt,i.checked)),y(),k(i.contentTemplate?-1:2))},dependencies:[at,lt,F],encapsulation:2,changeDetection:0})}return e})(),Ue=(()=>{class e{static \u0275fac=function(o){return new(o||e)};static \u0275mod=x({type:e});static \u0275inj=L({imports:[It,F,F]})}return e})();var Gt=["data-p-icon","angle-right"],We=(()=>{class e extends f{static \u0275fac=(()=>{let t;return function(i){return(t||(t=r(e)))(i||e)}})();static \u0275cmp=p({type:e,selectors:[["","data-p-icon","angle-right"]],features:[l],attrs:Gt,decls:1,vars:0,consts:[["d","M5.25 11.1728C5.14929 11.1694 5.05033 11.1455 4.9592 11.1025C4.86806 11.0595 4.78666 10.9984 4.72 10.9228C4.57955 10.7822 4.50066 10.5916 4.50066 10.3928C4.50066 10.1941 4.57955 10.0035 4.72 9.86283L7.72 6.86283L4.72 3.86283C4.66067 3.71882 4.64765 3.55991 4.68275 3.40816C4.71785 3.25642 4.79932 3.11936 4.91585 3.01602C5.03238 2.91268 5.17819 2.84819 5.33305 2.83149C5.4879 2.81479 5.64411 2.84671 5.78 2.92283L9.28 6.42283C9.42045 6.56346 9.49934 6.75408 9.49934 6.95283C9.49934 7.15158 9.42045 7.34221 9.28 7.48283L5.78 10.9228C5.71333 10.9984 5.63193 11.0595 5.5408 11.1025C5.44966 11.1455 5.35071 11.1694 5.25 11.1728Z","fill","currentColor"]],template:function(o,i){o&1&&(g(),b(0,"path",0))},encapsulation:2})}return e})();export{Te as a,We as b,oe as c,me as d,ve as e,It as f,Ue as g,Tt as h,Rt as i,Lt as j,Et as k,xt as l,Ct as m};
