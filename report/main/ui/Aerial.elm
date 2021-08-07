module Aerial exposing (main)

import Browser exposing (Document, UrlRequest(..))
import Browser.Navigation as Navigation
import Html exposing (Html)
import Html.Attributes as HA
import Json.Decode as Decode
import OverviewPage exposing (viewOverviewPage)
import Report exposing (..)
import Results.ReportPage exposing (viewComponentNotFoundPage, viewComponentPage)
import Theme exposing (Theme(..))
import UI exposing (..)
import Url exposing (Url)


main : Program Flags Model Msg
main =
    Browser.application
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        , onUrlChange = ChangedUrl
        , onUrlRequest = ClickedLink
        }


type alias Model =
    { navKey : Navigation.Key
    , app : String
    , theme : Theme
    , page : Page
    , report : Report
    , ui : UI
    }


type Page
    = OverviewPage
    | ComponentPage String


type alias Flags =
    { report : Decode.Value }


init : Flags -> Url -> Navigation.Key -> ( Model, Cmd Msg )
init flags url key =
    let
        report : Report
        report =
            case Decode.decodeValue Report.decoder flags.report of
                Ok value ->
                    value

                Err error ->
                    { components = [], examples = [], crosscuts = [], variables = [] }

        model : Model
        model =
            { navKey = key
            , app = "Allude"
            , theme = Light
            , page = OverviewPage
            , report = report
            , ui = initUI
            }
    in
    ( changedUrl url model, Cmd.none )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg ({ navKey } as model) =
    case msg of
        ChangedUrl url ->
            ( changedUrl url model, Cmd.none )

        ClickedLink urlRequest ->
            case urlRequest of
                Internal url ->
                    ( model, clickedLink url navKey )

                External string ->
                    ( model, Cmd.none )

        ToggleExample example ->
            ( { model | ui = toggleExample example model.ui }, Cmd.none )

        ToggleTheme ->
            ( { model | theme = Theme.toggleTheme model.theme }, Cmd.none )


subscriptions : Model -> Sub Msg
subscriptions model =
    Sub.none


view : Model -> Document Msg
view model =
    { title = "Aerial report for " ++ model.app
    , body =
        [ Html.div
            [ HA.class "app"
            , Theme.class model.theme
            ]
            [ case model.page of
                OverviewPage ->
                    viewOverviewPage model.app model.report.components

                ComponentPage name ->
                    case findComponent name model.report.components of
                        Just component ->
                            viewComponentPage model.app component (examplesForComponent name model.report.examples) model.ui.expanded

                        Nothing ->
                            viewComponentNotFoundPage name
            ]
        ]
    }


findComponent : String -> List Component -> Maybe Component
findComponent name components =
    List.filter (.component >> (==) name) components |> List.head


examplesForComponent : String -> List Example -> List Example
examplesForComponent component examples =
    List.filter (.component >> (==) component) examples


changedUrl : Url -> Model -> Model
changedUrl url ({ navKey } as model) =
    if "/" == url.path then
        { model | page = OverviewPage }

    else if String.startsWith "/component/" url.path then
        { model | page = ComponentPage (String.replace "/component/" "" url.path) }

    else
        model


clickedLink : Url -> Navigation.Key -> Cmd Msg
clickedLink url navKey =
    if "/" == url.path then
        Navigation.pushUrl navKey "/"

    else if String.startsWith "/component/" url.path then
        Navigation.pushUrl navKey url.path

    else
        Cmd.none
