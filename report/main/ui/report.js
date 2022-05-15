import {Elm} from "./Aerial.elm";
import report from "../../../build/report.json";

let node = document.querySelector("#app");
Elm.Aerial.init({
  node: node,
  flags: {report: report}
});
