var elem = arguments[0];
var result = isVisible(elem);

function isVisible(obj)
{
    if (!obj) return false;
    if (obj == document) return true;
    
    if (window.getComputedStyle) {
        var style = window.getComputedStyle(obj, null);
        if (style.display == 'none') return false;
        if (style.visibility == 'hidden') return false;
    }
    
    return isVisible(obj.parentNode);
}