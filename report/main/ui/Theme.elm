module Theme exposing (..)

import Html exposing (Html)
import Html.Attributes as HA
import Html.Events


type Theme
    = Light
    | Dark


type Msg
    = ToggleTheme


viewSwitcher : Html Msg
viewSwitcher =
    Html.div
        [ HA.class "theme-switcher"
        , Html.Events.onClick ToggleTheme
        ]
        []


class theme =
    HA.class <|
        "theme-"
            ++ (case theme of
                    Light ->
                        "light"

                    Dark ->
                        "dark"
               )


toggleTheme : Theme -> Theme
toggleTheme theme =
    case theme of
        Light ->
            Dark

        Dark ->
            Light
