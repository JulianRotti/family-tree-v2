"use client"

import { motion, AnimatePresence } from "framer-motion"
import { useState, useMemo } from "react"
import { familyMembers, getMemberById, getDescendantsFromHead, getGenerationsFromHead, type FamilyMember } from "@/lib/mock-data"
import { cn } from "@/lib/utils"
import { User, MapPin, Search, ChevronDown, Users, Layers, Sparkles } from "lucide-react"
import Image from "next/image"
import { GenderIndicator } from "@/components/gender-indicator"

const locationCoords: Record<string, { x: number; y: number }> = {
  'Florence, Italy': { x: 75, y: 35 },
  'Rome, Italy': { x: 70, y: 45 },
  'New York, USA': { x: 25, y: 40 },
  'Boston, USA': { x: 28, y: 35 },
  'Philadelphia, USA': { x: 26, y: 42 },
  'Brooklyn, USA': { x: 26, y: 41 },
  'Chicago, USA': { x: 18, y: 38 },
  'Connecticut, USA': { x: 27, y: 37 },
}

interface MapPointProps {
  member: FamilyMember
  x: number
  y: number
  isSelected: boolean
  onSelect: (id: string) => void
  delay?: number
}

function MapPoint({ member, x, y, isSelected, onSelect, delay = 0 }: MapPointProps) {
  return (
    <motion.button
      onClick={() => onSelect(member.id)}
      initial={{ scale: 0, opacity: 0 }}
      animate={{ scale: 1, opacity: 1 }}
      exit={{ scale: 0, opacity: 0 }}
      transition={{ delay, type: "spring", stiffness: 300, damping: 20 }}
      whileHover={{ scale: 1.3 }}
      className={cn(
        "absolute flex items-center justify-center transition-all duration-200",
        isSelected ? "z-20" : "z-10"
      )}
      style={{ left: `${x}%`, top: `${y}%`, transform: 'translate(-50%, -50%)' }}
    >
      <div
        className={cn(
          "w-3 h-3 rounded-full transition-all duration-200",
          isSelected
            ? "bg-terracotta shadow-lg shadow-terracotta/50 ring-2 ring-terracotta ring-offset-2 ring-offset-background"
            : "bg-dusty-rose/80 hover:bg-dusty-rose"
        )}
      />
      {isSelected && (
        <motion.div
          initial={{ opacity: 0, y: 5 }}
          animate={{ opacity: 1, y: 0 }}
          className="absolute -bottom-8 left-1/2 -translate-x-1/2 whitespace-nowrap bg-card px-2 py-1 rounded-md shadow-md border border-border"
        >
          <span className="text-xs font-medium text-foreground">
            {member.firstName} {member.lastName}
          </span>
        </motion.div>
      )}
    </motion.button>
  )
}

function HeadSelector({ 
  selectedId, 
  onSelect 
}: { 
  selectedId: string
  onSelect: (id: string) => void 
}) {
  const [isOpen, setIsOpen] = useState(false)
  const [search, setSearch] = useState('')
  
  const selectedMember = getMemberById(selectedId)
  
  const filteredMembers = useMemo(() => {
    if (!search.trim()) return familyMembers
    const query = search.toLowerCase()
    return familyMembers.filter(m => 
      m.firstName.toLowerCase().includes(query) ||
      m.lastName.toLowerCase().includes(query)
    )
  }, [search])

  return (
    <div className="relative">
      <button
        onClick={() => setIsOpen(!isOpen)}
        className="flex items-center gap-3 px-4 py-2.5 bg-card border border-border rounded-xl hover:border-dusty-rose transition-colors min-w-[240px]"
      >
        {selectedMember?.imageUrl ? (
          <Image
            src={selectedMember.imageUrl}
            alt={selectedMember.firstName}
            width={32}
            height={32}
            className="w-8 h-8 rounded-full object-cover"
          />
        ) : (
          <div className="w-8 h-8 rounded-full bg-secondary flex items-center justify-center">
            <User className="w-4 h-4 text-muted-foreground" />
          </div>
        )}
        <div className="flex-1 text-left">
          <p className="text-sm font-medium text-foreground">
            {selectedMember ? `${selectedMember.firstName} ${selectedMember.lastName}` : 'Select head'}
          </p>
          <p className="text-xs text-muted-foreground">Starting point</p>
        </div>
        <ChevronDown className={cn(
          "w-4 h-4 text-muted-foreground transition-transform",
          isOpen && "rotate-180"
        )} />
      </button>
      
      <AnimatePresence>
        {isOpen && (
          <motion.div
            initial={{ opacity: 0, y: -8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            className="absolute top-full left-0 right-0 mt-2 bg-card border border-border rounded-xl shadow-lg overflow-hidden z-50"
          >
            <div className="p-2 border-b border-border">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <input
                  type="text"
                  placeholder="Search members..."
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  className="w-full pl-9 pr-3 py-2 bg-secondary rounded-lg text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-1 focus:ring-terracotta"
                  autoFocus
                />
              </div>
            </div>
            <div className="max-h-64 overflow-y-auto">
              {filteredMembers.map(member => (
                <button
                  key={member.id}
                  onClick={() => {
                    onSelect(member.id)
                    setIsOpen(false)
                    setSearch('')
                  }}
                  className={cn(
                    "w-full flex items-center gap-3 px-3 py-2.5 hover:bg-secondary transition-colors",
                    member.id === selectedId && "bg-terracotta/10"
                  )}
                >
                  {member.imageUrl ? (
                    <Image
                      src={member.imageUrl}
                      alt={member.firstName}
                      width={28}
                      height={28}
                      className="w-7 h-7 rounded-full object-cover"
                    />
                  ) : (
                    <div className="w-7 h-7 rounded-full bg-muted flex items-center justify-center">
                      <User className="w-3.5 h-3.5 text-muted-foreground" />
                    </div>
                  )}
                  <div className="flex-1 text-left">
                    <p className="text-sm text-foreground flex items-center gap-1">
                      {member.firstName} {member.lastName}
                      <GenderIndicator gender={member.gender} />
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {new Date(member.birthDate).getFullYear()}
                      {member.deathDate ? ` – ${new Date(member.deathDate).getFullYear()}` : ''}
                    </p>
                  </div>
                  {member.id === selectedId && (
                    <span className="text-xs text-terracotta font-medium">Selected</span>
                  )}
                </button>
              ))}
              {filteredMembers.length === 0 && (
                <p className="px-4 py-6 text-sm text-muted-foreground text-center">
                  No members found
                </p>
              )}
            </div>
          </motion.div>
        )}
      </AnimatePresence>
      
      {/* Backdrop */}
      {isOpen && (
        <div 
          className="fixed inset-0 z-40" 
          onClick={() => {
            setIsOpen(false)
            setSearch('')
          }} 
        />
      )}
    </div>
  )
}

export function MapView() {
  const [headId, setHeadId] = useState<string>('1')
  const [selectedYear, setSelectedYear] = useState(1920)
  const [selectedMemberId, setSelectedMemberId] = useState<string | null>(null)

  const headMember = getMemberById(headId)

  // Get all descendants from the head
  const descendants = useMemo(() => getDescendantsFromHead(headId), [headId])

  // Calculate stats for descendants
  const stats = useMemo(() => {
    const generations = getGenerationsFromHead(headId)
    const livingCount = descendants.filter(m => !m.deathDate).length
    return { generations, totalCount: descendants.length, livingCount }
  }, [headId, descendants])

  // Get year range based on head's birth year
  const headBirthYear = headMember ? new Date(headMember.birthDate).getFullYear() : 1850
  const minYear = headBirthYear
  const maxYear = 2025

  // Filter descendants alive in selected year
  const membersAlive = useMemo(() => {
    return descendants.filter(member => {
      const birthYear = new Date(member.birthDate).getFullYear()
      const deathYear = member.deathDate ? new Date(member.deathDate).getFullYear() : 2025
      return birthYear <= selectedYear && selectedYear <= deathYear
    })
  }, [descendants, selectedYear])

  // Group members by location with staggered delays for animation
  const membersByLocation = useMemo(() => {
    const groups: Record<string, { member: FamilyMember; delay: number }[]> = {}
    let totalIndex = 0
    
    // Sort members by birth year for chronological animation
    const sortedMembers = [...membersAlive].sort((a, b) => 
      new Date(a.birthDate).getFullYear() - new Date(b.birthDate).getFullYear()
    )
    
    sortedMembers.forEach(member => {
      const loc = member.birthPlace || 'Unknown'
      if (!groups[loc]) groups[loc] = []
      groups[loc].push({ member, delay: totalIndex * 0.05 })
      totalIndex++
    })
    return groups
  }, [membersAlive])

  // Update selected year when head changes to start at their birth
  const handleHeadChange = (newHeadId: string) => {
    setHeadId(newHeadId)
    const newHead = getMemberById(newHeadId)
    if (newHead) {
      setSelectedYear(new Date(newHead.birthDate).getFullYear())
    }
    setSelectedMemberId(null)
  }

  return (
    <div className="h-full flex flex-col">
      {/* Header with Stats */}
      <div className="px-6 py-5 border-b border-border bg-card/50 backdrop-blur-sm">
        <div className="flex flex-col lg:flex-row lg:items-center gap-4">
          {/* Head Selector */}
          <div className="flex-shrink-0">
            <p className="text-xs text-muted-foreground uppercase tracking-wide mb-2">Start from</p>
            <HeadSelector selectedId={headId} onSelect={handleHeadChange} />
          </div>
          
          {/* Stats */}
          <div className="flex-1 flex items-center gap-6 lg:justify-end">
            <div className="flex items-center gap-2">
              <div className="w-9 h-9 rounded-lg bg-sage/10 flex items-center justify-center">
                <Layers className="w-4 h-4 text-sage" />
              </div>
              <div>
                <p className="text-lg font-serif font-semibold text-foreground">{stats.generations}</p>
                <p className="text-xs text-muted-foreground">Generations</p>
              </div>
            </div>
            
            <div className="flex items-center gap-2">
              <div className="w-9 h-9 rounded-lg bg-terracotta/10 flex items-center justify-center">
                <Users className="w-4 h-4 text-terracotta" />
              </div>
              <div>
                <p className="text-lg font-serif font-semibold text-foreground">{stats.totalCount}</p>
                <p className="text-xs text-muted-foreground">Descendants</p>
              </div>
            </div>
            
            <div className="flex items-center gap-2">
              <div className="w-9 h-9 rounded-lg bg-dusty-rose/10 flex items-center justify-center">
                <Sparkles className="w-4 h-4 text-dusty-rose" />
              </div>
              <div>
                <p className="text-lg font-serif font-semibold text-foreground">{stats.livingCount}</p>
                <p className="text-xs text-muted-foreground">Living</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Map Area */}
      <div className="flex-1 relative overflow-hidden">
        <div className="absolute inset-0">
          <Image
            src="https://hebbkx1anhila5yf.public.blob.vercel-storage.com/ChatGPT%20Image%20Apr%2011%2C%202026%2C%2010_13_42%20AM-jA3q5G5RjTNXOIu2dybtvcV3AAXANE.png"
            alt="Ancient map background"
            fill
            className="object-contain opacity-30"
          />
          <div className="absolute inset-0 bg-gradient-to-b from-background/40 via-transparent to-background/60" />
        </div>

        {/* Map Points */}
        <div className="relative w-full h-full">
          <AnimatePresence mode="popLayout">
            {Object.entries(membersByLocation).map(([location, members]) => {
              const coords = locationCoords[location]
              if (!coords) return null

              return members.map(({ member, delay }, idx) => (
                <MapPoint
                  key={member.id}
                  member={member}
                  x={coords.x + (idx % 3) * 2}
                  y={coords.y + Math.floor(idx / 3) * 3}
                  isSelected={selectedMemberId === member.id}
                  onSelect={setSelectedMemberId}
                  delay={delay}
                />
              ))
            })}
          </AnimatePresence>
        </div>

      </div>

      {/* Time Slider */}
      <div className="px-6 py-6 border-t border-border bg-card/80 backdrop-blur-sm">
        <div className="flex items-center gap-4">
          <span className="text-sm text-muted-foreground w-12">{minYear}</span>
          <div className="flex-1 relative">
            <input
              type="range"
              min={minYear}
              max={maxYear}
              value={selectedYear}
              onChange={(e) => setSelectedYear(parseInt(e.target.value))}
              className="w-full h-2 bg-secondary rounded-full appearance-none cursor-pointer
                [&::-webkit-slider-thumb]:appearance-none
                [&::-webkit-slider-thumb]:w-5
                [&::-webkit-slider-thumb]:h-5
                [&::-webkit-slider-thumb]:bg-terracotta
                [&::-webkit-slider-thumb]:rounded-full
                [&::-webkit-slider-thumb]:cursor-pointer
                [&::-webkit-slider-thumb]:shadow-md
                [&::-webkit-slider-thumb]:transition-transform
                [&::-webkit-slider-thumb]:hover:scale-110
                [&::-moz-range-thumb]:w-5
                [&::-moz-range-thumb]:h-5
                [&::-moz-range-thumb]:bg-terracotta
                [&::-moz-range-thumb]:rounded-full
                [&::-moz-range-thumb]:border-0
                [&::-moz-range-thumb]:cursor-pointer"
            />
          </div>
          <span className="text-sm text-muted-foreground w-12 text-right">{maxYear}</span>
        </div>
        <div className="text-center mt-4">
          <span className="text-3xl font-serif text-terracotta">{selectedYear}</span>
          <p className="text-sm text-muted-foreground mt-1">
            {membersAlive.length} {membersAlive.length === 1 ? 'person' : 'people'} alive
          </p>
        </div>
      </div>

      {/* People Alive List */}
      <div className="px-6 py-4 border-t border-border bg-card/50 max-h-40 overflow-auto">
        <h3 className="text-sm font-medium text-foreground mb-3 flex items-center gap-2">
          <User className="w-4 h-4 text-sage" />
          People alive in {selectedYear}
        </h3>
        <div className="flex flex-wrap gap-2">
          {membersAlive.map(member => (
            <button
              key={member.id}
              onClick={() => setSelectedMemberId(member.id)}
              className={cn(
                "flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm transition-all duration-200",
                selectedMemberId === member.id
                  ? "bg-terracotta/20 text-foreground border border-terracotta"
                  : "bg-secondary text-muted-foreground hover:text-foreground hover:bg-secondary/80"
              )}
            >
              <div className="w-5 h-5 rounded-full overflow-hidden bg-muted flex items-center justify-center shrink-0">
                {member.imageUrl ? (
                  <Image
                    src={member.imageUrl}
                    alt={member.firstName}
                    width={20}
                    height={20}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <User className="w-3 h-3 text-muted-foreground" />
                )}
              </div>
              {member.firstName} {member.lastName}
              <GenderIndicator gender={member.gender} />
            </button>
          ))}
          {membersAlive.length === 0 && (
            <p className="text-sm text-muted-foreground italic">No descendants alive in this year</p>
          )}
        </div>
      </div>
    </div>
  )
}
