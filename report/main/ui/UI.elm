module UI exposing (..)

import Browser exposing (UrlRequest)
import Set exposing (Set)
import Url exposing (Url)


type Msg
    = ChangedUrl Url
    | ClickedLink UrlRequest
    | ToggleTheme
    | ToggleExample String


type alias UI =
    { expanded : Set String
    }


initUI : UI
initUI =
    { expanded = Set.empty
    }


toggleExample : String -> UI -> UI
toggleExample example ui =
    if Set.member example ui.expanded then
        { ui | expanded = Set.remove example ui.expanded }

    else
        { ui | expanded = Set.insert example ui.expanded }
