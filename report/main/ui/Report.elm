module Report exposing (..)

import Dict exposing (Dict)
import Json.Decode as Decode exposing (Decoder)



-- sync report/main/org/aerial/report/Report.kt


type alias Report =
    { components : List Component
    , examples : List Example
    , crosscuts : List Crosscut
    , variables : List Variable
    }


type alias Component =
    { component : String
    , desc : String
    , tags : List String
    , features : List String
    , file : String
    , line : Int
    }


type alias Example =
    { component : String
    , feature : String
    , example : String
    , variables : Dict String String
    , tags : List String
    , type_ : ExampleType
    , locations : List Loc
    }


type ExampleType
    = Normal
    | HowTo
    | Todo


type alias Loc =
    { file : String
    , line : Int
    }


type alias Crosscut =
    { name : String
    , file : String
    , line : Int
    }


type alias Variable =
    { name : String
    , values : List String
    , file : String
    , line : Int
    }


decoder : Decoder Report
decoder =
    Decode.map4 Report
        (Decode.field "components" <| Decode.list componentDecoder)
        (Decode.field "examples" <| Decode.list exampleDecoder)
        (Decode.field "crosscuts" <| Decode.list crosscutDecoder)
        (Decode.field "variables" <| Decode.list variableDecoder)


componentDecoder : Decoder Component
componentDecoder =
    Decode.map6 Component
        (Decode.field "component" <| Decode.string)
        (Decode.field "desc" <| Decode.string)
        (Decode.field "tags" <| Decode.list Decode.string)
        (Decode.field "features" <| Decode.list Decode.string)
        (Decode.field "file" <| Decode.string)
        (Decode.field "line" <| Decode.int)


exampleDecoder : Decoder Example
exampleDecoder =
    Decode.map7 Example
        (Decode.field "component" <| Decode.string)
        (Decode.field "feature" <| Decode.string)
        (Decode.field "example" <| Decode.string)
        (Decode.field "variables" <| Decode.dict Decode.string)
        (Decode.field "tags" <| Decode.list Decode.string)
        (Decode.field "type" <| exampleTypeDecoder)
        (Decode.field "locations" <| Decode.list locDecoder)


exampleTypeDecoder : Decoder ExampleType
exampleTypeDecoder =
    Decode.string
        |> Decode.andThen
            (\val ->
                case val |> String.toUpper of
                    "EXAMPLE" ->
                        Decode.succeed Normal

                    "HOW_TO" ->
                        Decode.succeed HowTo

                    "TODO" ->
                        Decode.succeed Todo

                    _ ->
                        Decode.fail <| "Could not parse example type: " ++ val
            )


crosscutDecoder : Decoder Crosscut
crosscutDecoder =
    Decode.map3 Crosscut
        (Decode.field "name" <| Decode.string)
        (Decode.field "file" <| Decode.string)
        (Decode.field "line" <| Decode.int)


variableDecoder : Decoder Variable
variableDecoder =
    Decode.map4 Variable
        (Decode.field "name" <| Decode.string)
        (Decode.field "values" <| Decode.list Decode.string)
        (Decode.field "file" <| Decode.string)
        (Decode.field "line" <| Decode.int)


locDecoder : Decoder Loc
locDecoder =
    Decode.map2 Loc
        (Decode.field "file" <| Decode.string)
        (Decode.field "line" <| Decode.int)
