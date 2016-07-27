
importClass(Packages.java.io.FileReader);
importClass(Packages.java.io.BufferedReader);

function read(fileName) {
    var lines;
    var line;
    var reader;
    var i = 0;
    
    lines = new Array();
    reader = new BufferedReader(new FileReader(fileName));
    while ((line = reader.readLine()) != null) {
        lines[i] = line;
        i++;
    }
    return lines;
}

for (var i in arguments) {
    var lines = read(arguments[i]);
    for (var j in lines) {
        print(lines[j]);
    }
}
