#macro {
    getter(%value(2-2)!%)
} => {
    public %value(0-1)% get ## %value(1-2)%() {
        return this.%value(1-2)%;
    }
}

#macro {
    getters(%val(2-2)!%, %args[,](2-2)!%)
} => {
    getter(%val%) getters(%args[,]%)
}

#macro {
    getters(%val(2-2)!%)
} => {
    getter(%val%)
}

#macro {
    setter(%value(2-2)!%)
} => {
    public void set ## %value(1-2)%(%value%) {
        this.%value(1-2)% = %value(1-2)%;
    }
}

#macro {
    setters(%val(2-2)!%, %args[,](2-2)!%)
} => {
    setter(%val%) setters(%args[,]%)
}

#macro {
    setters(%val(2-2)!%)
} => {
    setter(%val%)
}

#macro {
    field(%val(2-2)!%)
} => {
    private %val%;
}

#macro {
    fields(%val(2-2)!%, %args[,](2-2)%)
} => {
    field(%val%) fields(%args[,]%)
}

#macro {
    fields(%val(2-2)!%)
} => {
    field(%val%)
}

#macro {
    data class %name(1-1)% (%values[,](2-2)%)
} => {
    class %name% {
        fields (%values[,]%)
        getters(%values[,]%)
        setters(%values[,]%)
    }
}

public data class Token (String value, Rule rule)
